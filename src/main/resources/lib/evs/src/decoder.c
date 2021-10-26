/*====================================================================================
    EVS Codec 3GPP TS26.443 May 28, 2020. Version 12.12.0 / 13.8.0 / 14.4.0 / 15.2.0 / 16.1.0
  ====================================================================================*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <assert.h>
#include "options.h"
#include "prot.h"
#include "cnst.h"
#include "rom_com.h"
#include "g192.h"

/*------------------------------------------------------------------------------------------*
 * Global variables
 *------------------------------------------------------------------------------------------*/
//long decFrame = 0;                 /* Counter of frames */
//int is_evs_dec_init = 0;

/*------------------------------------------------------------------------------------------*
 * Main decoder function
 *------------------------------------------------------------------------------------------*/

//void decode( int argc, char** argv, FILE *f_stream, FILE *f_synth )
void decode( int argc, char** argv, char *f_stream, char *f_synth, int data_count )
{
    short output_frame, dec_delay, zero_pad;
    short quietMode = 0;
    short noDelayCmp = 0;
    float output[L_FRAME48k];           /* 'float' buffer for output synthesis */
    short data[320];             /* 'short' buffer for output synthesis */
#ifdef SUPPORT_JBM_TRACEFILE
    char *jbmTraceFileName = NULL;      /* VOIP tracefile name */
#endif
    char *jbmFECoffsetFileName = NULL;  /* VOIP tracefile name */

    Decoder_State st;                  /* decoder state structure */

    st.cldfbAna = st.cldfbBPF = st.cldfbSyn = NULL;
    st.hFdCngDec = NULL;

    //if (is_evs_dec_init == 0) {
        /*if ( (st = (Decoder_State *) malloc( sizeof(Decoder_State) ) ) == NULL ) {
            fprintf(stderr, "Can not allocate memory for decoder state structure\n");
            exit(-1);
        }*/

        /* set to NULL, to avoid reading of uninitialized memory in case of early abort */
        /*st->cldfbAna = st->cldfbBPF = st->cldfbSyn = NULL;
        st->hFdCngDec = NULL;*/

        //is_evs_dec_init = 1;
    //}

    io_ini_dec( argc, argv, f_stream, f_synth,
                &quietMode, &noDelayCmp, &st,
#ifdef SUPPORT_JBM_TRACEFILE
                &jbmTraceFileName,
#endif
                &jbmFECoffsetFileName
              );

/*    if( st.Opt_VOIP ) {
        if( decodeVoip( &st, f_stream, f_synth,
#ifdef SUPPORT_JBM_TRACEFILE
                        jbmTraceFileName,
#endif
                        jbmFECoffsetFileName,
                        quietMode
                      ) != 0 ) {
            //free( st );
            fclose( f_synth );
            fclose( f_stream );
            return;
        }
    } else {*/
        init_decoder( &st );
        reset_indices_dec( &st );

        srand( (unsigned int) time(0) );

        /* output frame length */
        output_frame = (short)(st.output_Fs / 50);

        /*if (src_data_len < output_frame) {
            output_frame = src_data_len;
        }*/

        if( noDelayCmp == 0 ) {
            /* calculate the delay compensation to have the decoded signal aligned with the original input signal */
            /* the number of first output samples will be reduced by this amount */
            dec_delay = NS2SA(st.output_Fs, get_delay(DEC, st.output_Fs) + 0.5f);
        }
        else {
            dec_delay = 0;
        }
        zero_pad = dec_delay;

        //while( st.bitstreamformat==G192 ? read_indices( &st, f_stream, 0 ) : read_indices_mime( &st, f_stream, 0) ) {
        int cur_data_count = 0;
        for(; cur_data_count < data_count; cur_data_count++) {
            read_indices( &st, f_stream, 0 );
            f_stream += 4;

            memset(data, 0, 320);
            memcpy(data, f_stream, 320);
            f_stream += 320;

            /* run the main decoding routine */
            if ( st.codec_mode == MODE1 ) {
                if ( st.Opt_AMR_WB ) {
                    amr_wb_dec( &st, output );
                } else {
                    evs_dec( &st, output, FRAMEMODE_NORMAL );
                }
            }
            else {
                if( !st.bfi ) {
                    evs_dec( &st, output, FRAMEMODE_NORMAL );
                } else {
                    evs_dec( &st, output, FRAMEMODE_MISSING );
                }
            }

            syn_output( output, output_frame, data );
            if( st.ini_frame < MAX_FRAME_COUNTER ) {
                st.ini_frame++;
            }

            //if ( dec_delay == 0 ) {
                //fwrite( data, sizeof(short), output_frame, f_synth );
                memcpy(f_synth, data, 320);
                f_synth += 320;
            //}
            /* else {
                if ( dec_delay <= output_frame ) {
                    fwrite( &data[dec_delay], sizeof(short), output_frame - dec_delay, f_synth );
                    dec_delay = 0;
                } else {
                    dec_delay -= output_frame;
                }
            }*/

            //decFrame++;
            //fprintf( stdout, "output_frame=%d, decFrame=[%ld] | dst_data_len=[%zu]\n", output_frame, decFrame, dst_data_len );
        }

        //fflush( stderr );
        /*if (zero_pad > 0) {
            set_s( data, 0, zero_pad );
            fwrite( data, sizeof(short), zero_pad, f_synth );
        }*/

        destroy_decoder( &st );

        /*if (dst_data_len > 0) {
            result_data = (char*) malloc (dst_data_len * sizeof(char));
            memcpy(result_data, dst_data, dst_data_len);
            *result_data_len = dst_data_len;
        }*/
    //}

    //free( st );
}

