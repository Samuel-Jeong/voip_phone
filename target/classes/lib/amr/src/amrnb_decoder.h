#ifndef __AMRNB_DECODER_H__
#define __AMRNB_DECODER_H__

#include <stdio.h>

void start_dec_amrnb();
void stop_dec_amrnb();
void decode_amrnb( FILE* f_input, FILE* f_stream );

#endif

