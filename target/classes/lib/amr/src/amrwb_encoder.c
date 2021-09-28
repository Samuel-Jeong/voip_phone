#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "typedef.h"
#include "enc_if.h"

#define AMR_WB_MAGIC_NUMBER "#!AMR-WB\n"
static const int AMR_WB_ENC_DATA_LENGTH = 61;
static const int AMR_WB_DEC_DATA_LENGTH = 320;
static short block_size[16]={ 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };
//long enc_wb_frame = 0;

WB_enc_if_state *wb_enstate = NULL;
//int is_amrwb_enc_init = 0;

/////////////////////////////////////////////////////////////////////////////////////////////

void start_enc_amrwb() {
    if (wb_enstate == NULL) {
        wb_enstate = (WB_enc_if_state*) malloc (sizeof(WB_enc_if_state));
        if (wb_enstate == NULL) {
            return;
        }

        E_IF_init(wb_enstate);
    }
}

void stop_enc_amrwb() {
    if (wb_enstate != NULL) {
        free(wb_enstate);
        wb_enstate = NULL;
    }
}

void encode_amrwb( int req_mode, FILE* f_input, FILE* f_stream ) {
    if (wb_enstate == NULL) {
        return;
    }

    int i;

    //
    int dtx = 0;
    /*if (is_amrwb_enc_init == 0) {
        E_IF_init(&enstate);
        is_amrwb_enc_init = 1;
    }*/
    //

    //
    fwrite(AMR_WB_MAGIC_NUMBER, sizeof(char), strlen(AMR_WB_MAGIC_NUMBER), f_stream);

    short n_samples;
    short data[AMR_WB_DEC_DATA_LENGTH];
    while( (n_samples = (short)fread(data, sizeof(short int), AMR_WB_DEC_DATA_LENGTH, f_input)) > 0 ) {
        unsigned char serial_data[AMR_WB_ENC_DATA_LENGTH];
        int byte_counter = E_IF_encode(wb_enstate, req_mode, data, serial_data, dtx);
        fwrite(serial_data, sizeof(char), byte_counter, f_stream);

        //enc_wb_frame++;
        //printf("[ENC] test times: %ld, bytes: %d\n", enc_wb_frame, byte_counter);
    }
    //
}