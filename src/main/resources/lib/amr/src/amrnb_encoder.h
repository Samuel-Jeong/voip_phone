#ifndef __AMRNB_ENCODER_H__
#define __AMRNB_ENCODER_H__

#include <stdio.h>

void start_enc_amrnb();
void stop_enc_amrnb();
//void encode_amrnb( int req_mode, FILE* f_input, FILE** f_stream );
void encode_amrnb( int req_mode, char* f_input, char* f_stream );

#endif

