#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "typedef.h"
#include "dec_if.h"

static const int AMR_WB_HEADER_LENGTH = 9;
static const int AMR_WB_ENC_DATA_LENGTH = 61;
static const int AMR_WB_DEC_DATA_LENGTH = 320;
static const char AMR_WB_MAGIC_NUMBER[AMR_WB_HEADER_LENGTH] = {'#', '!', 'A', 'M', 'R', '-', 'W', 'B', '\n'};
static short block_size[16]={ 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };
//long dec_wb_frame = 0;

WB_dec_if_state *wb_destate = NULL;
//int is_amrwb_dec_init = 0;

/////////////////////////////////////////////////////////////////////////////////////////////

void start_dec_amrwb() {
    if (wb_destate == NULL) {
        wb_destate = (WB_dec_if_state*) malloc (sizeof(WB_dec_if_state));
        if (wb_destate == NULL) {
            return;
        }

        D_IF_init(wb_destate);
    }
}

void stop_dec_amrwb() {
    if (wb_destate != NULL) {
        free(wb_destate);
        wb_destate = NULL;
    }
}

void decode_amrwb( FILE* f_input, FILE* f_stream ) {
    if (wb_destate == NULL) {
        return;
    }

    int i;

    //
    /*if (is_amrwb_dec_init == 0) {
        D_IF_init(&destate);
        is_amrwb_dec_init = 1;
    }*/
    //

    //
    short n_samples;
    unsigned char header[AMR_WB_HEADER_LENGTH];
    n_samples = (short)fread(header, sizeof(char), AMR_WB_HEADER_LENGTH, f_input);
    if (n_samples <= 0) {
        return;
    } else {
        for (i = 0; i < AMR_WB_HEADER_LENGTH; i++) {
            if (header[i] != AMR_WB_MAGIC_NUMBER[i]) {
                return;
            }
        }
    }

    int read_size = 0;
    Word16 dec_mode;
    unsigned char data[AMR_WB_ENC_DATA_LENGTH];
    while( (n_samples = (short)fread(data, sizeof(char), AMR_WB_ENC_DATA_LENGTH, f_input)) > 0 ) {
        dec_mode = (data[0] >> 3) & 0x000F;
        if (dec_mode < 0 || dec_mode > 15) {
            fprintf(stderr, "[DEC] Error: dec_mode is unknown(%d)\n\n", dec_mode);
            break;
        }
        read_size = block_size[dec_mode];

        short speech[AMR_WB_DEC_DATA_LENGTH];
        D_IF_decode(wb_destate, data, speech, 0);
        fwrite(speech, sizeof(short int), AMR_WB_DEC_DATA_LENGTH, f_stream);

        //dec_wb_frame++;
        //printf("[DEC] test times: %ld, data[0]:%hhu, dec_mode:%d, bytes: %d\n", dec_wb_frame, data[0], dec_mode, read_size);
    }
    //
}
