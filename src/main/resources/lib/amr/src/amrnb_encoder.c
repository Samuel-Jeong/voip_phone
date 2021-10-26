#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "typedef.h"
#include "interf_enc.h"

#define AMR_NB_MAGIC_NUMBER "#!AMR\n"

static const int AMR_NB_HEADER_LENGTH = 6;
static const int AMR_NB_ENC_DATA_LENGTH = 32;
static const int AMR_NB_DEC_DATA_LENGTH = 160;
static short block_size[16]={ 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };
//long enc_nb_frame = 0;

enc_interface_State *nb_enstate = NULL;
//int is_amrnb_enc_init = 0;

/////////////////////////////////////////////////////////////////////////////////////////////

void start_enc_amrnb() {
    if (nb_enstate == NULL) {
        nb_enstate = (enc_interface_State*) malloc (sizeof(enc_interface_State));
        if (nb_enstate == NULL) {
            return;
        }

        Encoder_Interface_init(nb_enstate, 0);
    }
}

void stop_enc_amrnb() {
    if (nb_enstate != NULL) {
        free(nb_enstate);
        nb_enstate = NULL;
    }
}


// f_input len : 160
// f_stream len : 32

//void encode_amrnb( int req_mode, FILE* f_input, FILE** f_stream ) {
void encode_amrnb( int req_mode, char* f_input, char* f_stream ) {
    if (nb_enstate == NULL) {
        return;
    }

    int i;

    //
    /*int dtx = 0;
    if (is_amrnb_enc_init == 0) {
        Encoder_Interface_init(&enstate, dtx);
        is_amrnb_enc_init = 1;
    }*/
    //

    //
    memcpy(f_stream, AMR_NB_MAGIC_NUMBER, strlen(AMR_NB_MAGIC_NUMBER) * sizeof(char));
    f_stream += AMR_NB_HEADER_LENGTH;
    //fwrite(AMR_NB_MAGIC_NUMBER, sizeof(char), strlen(AMR_NB_MAGIC_NUMBER), *f_stream);

    //short n_samples;
    //short data[160];
    char data[320];
    //while( (n_samples = (short)fread(data, sizeof(short int), AMR_NB_DEC_DATA_LENGTH, f_input)) > 0 ) {
        memcpy(data, f_input, AMR_NB_DEC_DATA_LENGTH * sizeof(short int));

        //unsigned char serial_data[32];
        char serial_data[32];
        int byte_counter = Encoder_Interface_Encode(nb_enstate, req_mode, data, serial_data, 0);
        memcpy(f_stream, serial_data, AMR_NB_ENC_DATA_LENGTH * sizeof(char) );

        //fwrite(serial_data, sizeof(char), byte_counter, *f_stream);
        //memset(data, 0, AMR_NB_DEC_DATA_LENGTH);

        //enc_nb_frame++;
        //fprintf(stderr, "[ENC] test times: %ld, bytes: %d\n", enc_nb_frame, byte_counter);
    //}
    //
}
