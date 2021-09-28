/*====================================================================================
    EVS Codec 3GPP TS26.443 May 28, 2020. Version 12.12.0 / 13.8.0 / 14.4.0 / 15.2.0 / 16.1.0
  ====================================================================================*/

#include <math.h>
#include <stdio.h>
#include "cnst.h"
#include "prot.h"
#include "stat_com.h"
#include "assert.h"
#include "basop_util.h"

#ifndef int32
#define int32   int
#endif



/*---------------------------------------------------------------
  Ari 14 bits common routines
  -------------------------------------------------------------*/

/**
 * \brief 	Integer Multiply
 *
 * \param[i] r
 * \param[i] c
 *
 * \return r*c
 */
long mul_sbc_14bits(long r,long c)
{
    long temp;
    /*function in line*/
    temp = (((int32) r)*((int32) c))>>stat_bitsnew;
    return temp;

    /*function in line*/
}
