/*====================================================================================
    EVS Codec 3GPP TS26.443 May 28, 2020. Version 12.12.0 / 13.8.0 / 14.4.0 / 15.2.0 / 16.1.0
  ====================================================================================*/

#include "options.h"
#include "cnst.h"
#include "prot.h"
#include "rom_com.h"
#include <math.h>

/*--------------------------------------------------------------------------*
 * Local constants
 *--------------------------------------------------------------------------*/

#define ENV_STAB_SMO_HO 10                      /* number of hangover frames when switching from music to speech state */

/*--------------------------------------------------------------------------*/
/*  Function  env_stability()                                               */
/*  ~~~~~~~~~~~~~~~~~~~~~~~~~                                               */
/*                                                                          */
/*  Envelope stability measure                                              */
/*--------------------------------------------------------------------------*/

float env_stability(
    const short *ynrm,         /*i  : Norm vector for current frame          */
    const short nb_sfm,        /*i  : Number of sub-bands                    */
    short *mem_norm,     /*i/o: Norm vector memory from past frame     */
    float *mem_env_delta /*i/o: Envelope stability memory for smoothing*/
)
{
    Word16 env_delta;
    Word16 env_stab;
    Word16 tmp, tmp_stab;
    Word16 i;

    Word16 exp, exp2;
    Word32 L_tmp, L_env_delta;
    Word16 inv_nb_sfm;
    float env_stab_f;

    /* Calculate envelope stability parameter */
    L_env_delta = L_deposit_l(0);
    for (i = 0; i < nb_sfm; i++)
    {
        tmp = sub(mem_norm[i],ynrm[i]);
        L_env_delta = L_mac0(L_env_delta, tmp, tmp);
        mem_norm[i] = ynrm[i];
    }

    inv_nb_sfm = 19418; /* Q19 */
    if (nb_sfm == 26)
    {
        inv_nb_sfm = 20165; /* Q19 */
    }
    exp = norm_l(L_env_delta);
    L_env_delta = Mult_32_16(L_shl(L_env_delta, exp), inv_nb_sfm);  /* 0+exp+19-15 */

    L_tmp = Sqrt_l(L_env_delta, &exp2);     /* exp+4+31+exp2 */

    exp =  add(35, add(exp, exp2));
    if ( sub(s_and(exp, 1), 1) == 0 )
    {
        L_tmp = Mult_32_16(L_tmp, 23170);   /* 1/sqrt(2) in Q15 */
    }
    exp = shr(exp, 1);

    env_delta = round_fx(L_shl(L_tmp, sub(26, exp))); /* Q10 */

    L_tmp = L_mult0(26214, env_delta); /* 26214 is 0.1 in Q18. Q28 */
    L_tmp = L_mac(L_tmp, 29491, *mem_env_delta);   /* 29491 is 0.9 in Q15. Q28 */

    *mem_env_delta = round_fx(L_tmp);   /* Q12 */
    Overflow = 0;
    env_delta = round_fx(L_shl(L_tmp, 1));   /* Q13 */

    if (Overflow != 0) /* Saturated due to the above up-shifting operation. */
    {
        env_stab = stab_trans_fx[L_STAB_TBL-1];  /* The highest quantized index. */
        env_stab_f = ((float)env_stab)/32768.0f; /* Convert env_stab(Q15) to float */
        return env_stab_f;
    }

    /* If tmp_stab > (D_STAB_TBL*L_STAB_TBL + M_STAB_TBL), i.e., 0.103138*10+2.51757=3.603137,
     * the quantized index is equal to 9. Hence, we only need to worry about any tmpStab < 4.
     * In this case, Q13 is good enough.
     */
    tmp_stab = sub(env_delta, M_STAB_TBL_FX); /* in Q13 */
    tmp_stab = abs_s(tmp_stab);

    /* Table lookup for smooth transitions
     * First, find the quantization level, i, of tmpStab. */
#if L_STAB_TBL > 10
#error env_stability_fx: Use more efficient usquant()
#endif
    tmp_stab = sub(tmp_stab, HALF_D_STAB_TBL_FX); /* in Q13 */
    for (i = 0; i < L_STAB_TBL-1; i++)
    {
        if (tmp_stab < 0)
        {
            break;
        }
        else
        {
            tmp_stab = sub(tmp_stab, D_STAB_TBL_FX); /* in Q13 */
        }
    }

    env_stab = stab_trans_fx[i];
    if(sub(env_delta, M_STAB_TBL_FX) < 0)
    {
        env_stab = sub(0x7FFF,stab_trans_fx[i]);
    }

    env_stab_f = ((float)env_stab)/32768.0f; /* Convert env_stab(Q15) to float */

    return env_stab_f;
}

/*--------------------------------------------------------------------------*
 * env_stab_smo_fx()
 *
 *
 *--------------------------------------------------------------------------*/

float env_stab_smo(
    float env_stab,            /*i  : env_stab value                         */
    float *env_stab_state_p,   /*i/o: env_stab state probabilities           */
    short *ho_cnt              /*i/o: hangover counter for speech state      */
)
{
    short state, prev_state;
    float maxval, pp[NUM_ENV_STAB_PLC_STATES], pa[NUM_ENV_STAB_PLC_STATES];
    /* get previous state */
    prev_state = maximum(env_stab_state_p,NUM_ENV_STAB_PLC_STATES,&maxval);

    /* assume two states: speech(0), music(1) */
    /* set a posteriori likelihoods for the two states according to env_stab */
    env_stab = (env_stab - stab_trans[L_STAB_TBL-1])/(1-2*stab_trans[L_STAB_TBL-1]);
    pp[0] = 1.0f-env_stab;
    pp[1] = env_stab;

    /* calculate a priori likelihoods */
    pa[0] = dotp(env_stab_tp[0],env_stab_state_p,NUM_ENV_STAB_PLC_STATES);
    pa[1] = dotp(env_stab_tp[1],env_stab_state_p,NUM_ENV_STAB_PLC_STATES);

    /* multiply elementwise with a posteriori likelihoods */
    v_mult(pa,pp,env_stab_state_p,NUM_ENV_STAB_PLC_STATES);

    /* renormalize state probabilities */
    v_multc(env_stab_state_p,1.0f/sum_f(env_stab_state_p,NUM_ENV_STAB_PLC_STATES),env_stab_state_p,NUM_ENV_STAB_PLC_STATES);

    /* find maximum index as return value */
    state = maximum(env_stab_state_p,NUM_ENV_STAB_PLC_STATES,&maxval);

    /* apply some hangover for speech */
    if (state==0 && prev_state==1)
    {
        *ho_cnt=ENV_STAB_SMO_HO;
    }
    if (*ho_cnt>0)
    {
        pp[0]=1;
        pp[1]=0;
        (*ho_cnt)--;
    }

    return state;
}
