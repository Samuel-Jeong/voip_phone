#ifndef __AMRWB_DECODER_H__
#define __AMRWB_DECODER_H__

#include <stdio.h>

void start_dec_amrwb();
void stop_dec_amrwb();
void decode_amrwb( FILE* f_input, FILE* f_stream );

#endif

