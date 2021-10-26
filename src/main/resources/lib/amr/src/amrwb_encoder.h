#ifndef __AMRWB_ENCODER_H__
#define __AMRWB_ENCODER_H__

#include <stdio.h>

void start_enc_amrwb();
void stop_enc_amrwb();
//void encode_amrwb( int req_mode, FILE* f_input, FILE* f_stream );
void encode_amrwb( int req_mode, char* f_input, char* f_stream );

#endif

