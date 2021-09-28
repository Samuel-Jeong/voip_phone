#ifndef __AMRWB_ENCODER_H__
#define __AMRWB_ENCODER_H__

#include <stdio.h>

void start_enc_amrwb();
void stop_enc_amrwb();
void encode_amrwb( int req_mode, FILE* f_input, FILE* f_stream );

#endif

