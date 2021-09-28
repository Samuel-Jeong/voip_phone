#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>

#include "media_module_codec_amr_AmrManager.h"

#include "amrnb_encoder.h"
#include "amrnb_decoder.h"

#include "amrwb_encoder.h"
#include "amrwb_decoder.h"

//
static jbyte* jbyteArray2cstr( JNIEnv *jenv, jbyteArray javaBytes );
static jbyteArray cstr2jbyteArray(JNIEnv *jenv, const char *nativeStr, int dstDataLen);
static char* jstring_to_cstring( JNIEnv *env, jbyteArray javaBytes );
//

//
int is_enc_amrnb_started = 0;
int is_dec_amrnb_started = 0;

int is_enc_amrwb_started = 0;
int is_dec_amrwb_started = 0;
//

//
int main( int argc, char** argv )
{
	return 0;
}
//

/////////////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT jbyteArray JNICALL Java_media_module_codec_amr_AmrManager_enc_1amrnb (JNIEnv *jenv, jobject jobj, jint req_mode, jbyteArray javaBytes) {	size_t srcDataLen = (*jenv)->GetArrayLength( jenv, javaBytes );
    if (is_enc_amrnb_started == 0) {
        return NULL;
    }

	if (srcDataLen == 0) {
	    return NULL;
	}

    //fprintf(stderr, "[ENC] srcDataLen: %zu\n\n", srcDataLen);

    jbyte *srcData = jbyteArray2cstr( jenv, javaBytes );
    if (srcData == NULL) {
        return NULL;
    }

    FILE* f_input;
    if ( (f_input = fmemopen(srcData, srcDataLen, "rb")) == NULL ) {
    //if ( (f_input = fmemopen(javaBytes, srcDataLen, "rb")) == NULL ) {
        fprintf(stderr, "[ENC] Error: input could not be opened (%d)\n\n", errno);
        exit(1);
    }

    char *dstData;
	size_t dstDataLen;
    FILE* f_stream;
    if ( (f_stream = open_memstream( &dstData, &dstDataLen )) == NULL ) {
        fprintf(stderr, "[ENC] Error: output could not be opened (%d)\n\n", errno);
        exit(1);
    }

    encode_amrnb( req_mode, f_input, &f_stream );

    fclose(f_input);
    fclose(f_stream);
    (*jenv)->ReleaseByteArrayElements(jenv, javaBytes, srcData, 0);

    jbyteArray result;
    if (dstData != NULL) {
        //fprintf(stderr, "encode suc %zu (data=%s)\n", dstDataLen, dstData);
        result = cstr2jbyteArray( jenv, dstData, dstDataLen );
        //return dstData;
    } else {
        //printf("encode fail\n");
        return NULL;
    }

    /*size_t resultDataLen = (*jenv)->GetArrayLength( jenv, result );
    fprintf(stderr, "[ENC] resultDataLen: %zu\n\n", resultDataLen);*/

    return result;
}

JNIEXPORT void JNICALL Java_media_module_codec_amr_AmrManager_start_1enc_1amrnb (JNIEnv *jenv, jobject jobj) {
    if (is_enc_amrnb_started == 0) {
        start_enc_amrnb();
        is_enc_amrnb_started = 1;
    }
}

JNIEXPORT void JNICALL Java_media_module_codec_amr_AmrManager_stop_1enc_1amrnb (JNIEnv *jenv, jobject jobj) {
    if (is_enc_amrnb_started == 1) {
        stop_enc_amrnb();
        is_enc_amrnb_started = 0;
    }
}

JNIEXPORT jbyteArray JNICALL Java_media_module_codec_amr_AmrManager_dec_1amrnb (JNIEnv *jenv, jobject jobj, jint resultDataLen, jbyteArray javaBytes) {
    if (is_dec_amrnb_started == 0) {
        return NULL;
    }

    if (resultDataLen <= 0) {
        return NULL;
    }

	size_t srcDataLen = (*jenv)->GetArrayLength( jenv, javaBytes );
	if (srcDataLen == 0) {
	    return NULL;
	}

    //fprintf(stderr, "[DEC] srcDataLen: %zu\n\n", srcDataLen);

    jbyte *srcData = jbyteArray2cstr( jenv, javaBytes );
    if (srcData == NULL) {
        return NULL;
    }

    FILE* f_input;
    if ( (f_input = fmemopen(srcData, srcDataLen, "rb")) == NULL ) {
    //if ( (f_input = fmemopen(javaBytes, srcDataLen, "rb")) == NULL ) {
        fprintf(stderr, "[DEC] Error: input could not be opened (%d)\n\n", errno);
        exit(1);
    }

    char *dstData;
    size_t dstDataLen;
    FILE* f_stream;
    if ( (f_stream = open_memstream( &dstData, &dstDataLen )) == NULL ) {
        fprintf(stderr, "[DEC] Error: output could not be opened (%d)\n\n", errno);
        exit(1);
    }

    decode_amrnb( f_input, f_stream );

    fclose(f_input);
    fclose(f_stream);
    (*jenv)->ReleaseByteArrayElements(jenv, javaBytes, srcData, 0);

    jbyteArray result;
    if (dstData != NULL) {
        //printf("decode suc %zu\n", dstDataLen);
        result = cstr2jbyteArray( jenv, dstData, resultDataLen );
    } else {
        //printf("decode fail\n");
        return NULL;
    }

    /*size_t rrresultDataLen = (*jenv)->GetArrayLength( jenv, result );
    fprintf(stderr, "[DEC] resultDataLen: %zu\n\n", rrresultDataLen);*/

    return result;
}

JNIEXPORT void JNICALL Java_media_module_codec_amr_AmrManager_start_1dec_1amrnb (JNIEnv *jenv, jobject jobj) {
    if (is_dec_amrnb_started == 0) {
        start_dec_amrnb();
        is_dec_amrnb_started = 1;
    }
}

JNIEXPORT void JNICALL Java_media_module_codec_amr_AmrManager_stop_1dec_1amrnb (JNIEnv *jenv, jobject jobj) {
    if (is_dec_amrnb_started == 1) {
        stop_dec_amrnb();
        is_dec_amrnb_started = 0;
    }
}

JNIEXPORT jbyteArray JNICALL Java_media_module_codec_amr_AmrManager_enc_1amrwb (JNIEnv *jenv, jobject jobj, jint req_mode, jbyteArray javaBytes) {
    if (is_enc_amrwb_started == 0) {
        return NULL;
    }

	size_t srcDataLen = (*jenv)->GetArrayLength( jenv, javaBytes );
	if (srcDataLen == 0) {
	    return NULL;
	}

    jbyte *srcData = jbyteArray2cstr( jenv, javaBytes );
    if (srcData == NULL) {
        return NULL;
    }

    FILE* f_input;
    if ( (f_input = fmemopen(srcData, srcDataLen, "rb")) == NULL ) {
        fprintf(stderr, "[ENC] Error: input could not be opened (%d)\n\n", errno);
        exit(1);
    }

    FILE* f_stream;
    size_t dstDataLen;
    char* dstData;
    if ( (f_stream = open_memstream( &dstData, &dstDataLen )) == NULL ) {
        fprintf(stderr, "[ENC] Error: output could not be opened (%d)\n\n", errno);
        exit(1);
    }

    encode_amrwb( req_mode, f_input, f_stream );

    fclose(f_input);
    fclose(f_stream);
    (*jenv)->ReleaseByteArrayElements(jenv, javaBytes, srcData, 0);

    jbyteArray result;
    if (dstData != NULL) {
        //printf("encode suc %zu\n", dstDataLen);
        result = cstr2jbyteArray( jenv, dstData, dstDataLen );
    } else {
        //printf("encode fail\n");
        return NULL;
    }

    return result;
}

JNIEXPORT void JNICALL Java_media_module_codec_amr_AmrManager_start_1enc_1amrwb (JNIEnv *jenv, jobject jobj) {
    if (is_enc_amrwb_started == 0) {
        start_enc_amrwb();
        is_enc_amrwb_started = 1;
    }
}

JNIEXPORT void JNICALL Java_media_module_codec_amr_AmrManager_stop_1enc_1amrwb (JNIEnv *jenv, jobject jobj) {
    if (is_enc_amrwb_started == 1) {
        stop_enc_amrwb();
        is_enc_amrwb_started = 0;
    }
}

JNIEXPORT jbyteArray JNICALL Java_media_module_codec_amr_AmrManager_dec_1amrwb (JNIEnv *jenv, jobject jobj, jint resultDataLen, jbyteArray javaBytes) {
    if (is_dec_amrwb_started == 0) {
        return NULL;
    }

    if (resultDataLen <= 0) {
        return NULL;
    }

	size_t srcDataLen = (*jenv)->GetArrayLength( jenv, javaBytes );
	if (srcDataLen == 0) {
	    return NULL;
	}

    jbyte *srcData = jbyteArray2cstr( jenv, javaBytes );
    if (srcData == NULL) {
        return NULL;
    }

    FILE* f_input;
    if ( (f_input = fmemopen(srcData, srcDataLen, "rb")) == NULL ) {
        fprintf(stderr, "[DEC] Error: input could not be opened (%d)\n\n", errno);
        exit(1);
    }

    FILE* f_stream;
    size_t dstDataLen;
    char* dstData;
    if ( (f_stream = open_memstream( &dstData, &dstDataLen )) == NULL ) {
        fprintf(stderr, "[DEC] Error: output could not be opened (%d)\n\n", errno);
        exit(1);
    }

    decode_amrwb( f_input, f_stream );

    fclose(f_input);
    fclose(f_stream);
    (*jenv)->ReleaseByteArrayElements(jenv, javaBytes, srcData, 0);

    jbyteArray result;
    if (dstData != NULL) {
        //printf("decode suc %zu\n", dstDataLen);
        result = cstr2jbyteArray( jenv, dstData, dstDataLen );
    } else {
        //printf("decode fail\n");
        return NULL;
    }

    return result;
}

JNIEXPORT void JNICALL Java_media_module_codec_amr_AmrManager_start_1dec_1amrwb (JNIEnv *jenv, jobject jobj) {
    if (is_dec_amrwb_started == 0) {
        start_dec_amrwb();
        is_dec_amrwb_started = 1;
    }
}

JNIEXPORT void JNICALL Java_media_module_codec_amr_AmrManager_stop_1dec_1amrwb (JNIEnv *jenv, jobject jobj) {
    if (is_dec_amrwb_started == 1) {
        stop_dec_amrwb();
        is_dec_amrwb_started = 0;
    }
}

////////////////////////////////////////////////////////////////////////////////

static jbyte* jbyteArray2cstr(JNIEnv *jenv, jbyteArray javaBytes) {
    return (*jenv)->GetByteArrayElements( jenv, javaBytes, NULL );
}

////////////////////////////////////////////////////////////////////////////////

static jbyteArray cstr2jbyteArray(JNIEnv *jenv, const char *nativeStr, int dstDataLen) {
    jbyteArray javaBytes = (*jenv)->NewByteArray(jenv, dstDataLen);
    (*jenv)->SetByteArrayRegion(jenv, javaBytes, 0, dstDataLen, (jbyte*) nativeStr);
    return javaBytes;
}
