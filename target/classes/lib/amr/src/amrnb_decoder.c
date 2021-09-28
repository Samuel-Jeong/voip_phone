#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "typedef.h"
#include "interf_dec.h"

static const int AMR_NB_HEADER_LENGTH = 6;
static const int AMR_NB_ENC_DATA_LENGTH = 32;
static const int AMR_NB_DEC_DATA_LENGTH = 160;
static const char AMR_NB_MAGIC_NUMBER[AMR_NB_HEADER_LENGTH] = {'#', '!', 'A', 'M', 'R', '\n'};
static short block_size[16]={ 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };
//long dec_nb_frame = 0;

dec_interface_State *nb_destate = NULL;
//int is_amrnb_dec_init = 0;

/////////////////////////////////////////////////////////////////////////////////////////////

void start_dec_amrnb() {
    if (nb_destate == NULL) {
        nb_destate = (dec_interface_State*) malloc (sizeof(dec_interface_State));
        if (nb_destate == NULL) {
            return;
        }

        Decoder_Interface_init(nb_destate);
    }
}

void stop_dec_amrnb() {
    if (nb_destate != NULL) {
        free(nb_destate);
        nb_destate = NULL;
    }
}

void decode_amrnb( FILE* f_input, FILE* f_stream ) {
    if (nb_destate == NULL) {
        return;
    }

    int i;

    //
    /*int dtx = 0;
    if (is_amrnb_dec_init == 0) {
        Decoder_Interface_init(&destate);
        is_amrnb_dec_init = 1;
    }*/
    //

    //
    short n_samples;
    unsigned char header[AMR_NB_HEADER_LENGTH];
    n_samples = (short)fread(header, sizeof(char), AMR_NB_HEADER_LENGTH, f_input);
    if (n_samples <= 0) {
        return;
    } else {
        for (i = 0; i < AMR_NB_HEADER_LENGTH; i++) {
            if (header[i] != AMR_NB_MAGIC_NUMBER[i]) {
                return;
            }
        }
    }

    int read_size = 0;
    enum Mode dec_mode;
    unsigned char data[AMR_NB_ENC_DATA_LENGTH];
    while( (n_samples = (short)fread(data, sizeof(char), AMR_NB_ENC_DATA_LENGTH, f_input)) > 0 ) {
        dec_mode = (data[0] >> 3) & 0x000F;
        if (dec_mode < 0 || dec_mode > 15) {
            fprintf(stderr, "[DEC] Error: dec_mode is unknown(%d)\n\n", dec_mode);
            break;
        }
        read_size = block_size[dec_mode];

        short speech[AMR_NB_DEC_DATA_LENGTH];
        Decoder_Interface_Decode(nb_destate, data, speech, 0);

        fwrite(speech, sizeof(short int), AMR_NB_DEC_DATA_LENGTH, f_stream);
        memset(data, 0, AMR_NB_ENC_DATA_LENGTH);

        //dec_nb_frame++;
        //fprintf(stderr, "[DEC] test times: %ld, data[0]:%hhu, dec_mode:%d, bytes: %d\n", dec_nb_frame, data[0], dec_mode, read_size);
    }
    //
}
