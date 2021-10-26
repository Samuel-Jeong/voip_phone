#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>

#include "media_module_codec_evs_EvsManager.h"

#include "encoder.h"
#include "decoder.h"


//
static jbyte* jbyteArray2cstr( JNIEnv *jenv, jbyteArray javaBytes );
static jbyteArray cstr2jbyteArray(JNIEnv *jenv, const char *nativeStr, int dstDataLen);
static char* jstring_to_cstring( JNIEnv *env, jbyteArray javaBytes );
//

//
int main( int argc, char** argv )
{
	return 0;
}
//

/////////////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT jbyteArray JNICALL Java_media_module_codec_evs_EvsManager_enc_1evs (JNIEnv *jenv, jobject jobj, jobjectArray jargv, jbyteArray javaBytes) {
	size_t srcDataLen = (*jenv)->GetArrayLength( jenv, javaBytes );
	if (srcDataLen == 0) {
	    return NULL;
	}

    jsize argc = 5;
    //jsize argc = (*jenv)->GetArrayLength(jenv, jargv);
    char **argv = (char**) calloc (sizeof(char*), argc+1);
    if(!argv) {
        return NULL;
    }

    int i = 0;
    jobject textArray[5];
    for(; i < argc; i++) {
        jobject jtext = (*jenv)->GetObjectArrayElement(jenv, jargv, i);
        textArray[i] = jtext;

        char *utf8text = (*jenv)->GetStringUTFChars(jenv, jtext, NULL);
        argv[i] = utf8text;
    }

    jbyte *srcData = jbyteArray2cstr( jenv, javaBytes ); // 320 * 10
    if (srcData == NULL) {
        return NULL;
    }

    /*FILE* f_input;
    if ( (f_input = fmemopen(srcData, srcDataLen, "rb")) == NULL ) {
        fprintf(stderr, "[ENC] Error: input could not be opened (%d)\n\n", errno);
        exit(1);
    }*/

	size_t dstDataLen = 324 * 10;
	char dstData[324 * 10];
    //char *dstData; // 324
    /*FILE* f_stream;
    if ( (f_stream = open_memstream( &dstData, &dstDataLen )) == NULL ) {
        fprintf(stderr, "[ENC] Error: output could not be opened (%d)\n\n", errno);
        exit(1);
    }*/

    //encode( argc, argv, f_input, f_stream );
    encode( argc, argv, srcData, dstData, 10 );

    //fclose( f_input );
    //fclose( f_stream );
    (*jenv)->ReleaseByteArrayElements(jenv, javaBytes, srcData, 0);

    for (i = 0; i < argc; i++) {
        (*jenv)->ReleaseStringUTFChars(jenv, textArray[i], argv[i]);
    }

    jbyteArray result;
    //if (dstData != NULL) {
        //printf("encode suc %zu\n", dstDataLen);
        result = cstr2jbyteArray( jenv, dstData, dstDataLen );
    //} else {
        //printf("encode fail\n");
        //return NULL;
    //}

    return result;
}

JNIEXPORT jbyteArray JNICALL Java_media_module_codec_evs_EvsManager_dec_1evs (JNIEnv *jenv, jobject jobj, jobjectArray jargv, jint resultDataLen, jbyteArray javaBytes) {
    if (resultDataLen <= 0) {
        return NULL;
    }

	size_t srcDataLen = (*jenv)->GetArrayLength( jenv, javaBytes );
	if (srcDataLen == 0) {
	    return NULL;
	}

    jsize argc = (*jenv)->GetArrayLength(jenv, jargv);
    char **argv = (char**) calloc (sizeof(char*), argc+1);
    if(!argv) {
        return NULL;
    }

    int i = 0;
    jobject textArray[4];
    for(; i < argc; i++) {
        jobject jtext = (*jenv)->GetObjectArrayElement(jenv, jargv, i);
        textArray[i] = jtext;

        char *utf8text = (*jenv)->GetStringUTFChars(jenv, jtext, NULL);
        argv[i] = utf8text;
    }

    jbyte *srcData = jbyteArray2cstr( jenv, javaBytes ); // 324
    if (srcData == NULL) {
        return NULL;
    }

    /*FILE* f_input;
    if ( (f_input = fmemopen(srcData, srcDataLen, "rb")) == NULL ) {
        fprintf(stderr, "[DEC] Error: input could not be opened (%d)\n\n", errno);
        exit(1);
    }*/

    size_t dstDataLen = 320 * 10;
    char dstData[320 * 10];
    //char *dstData; // 320
    /*FILE* f_stream;
    if ( (f_stream = open_memstream( &dstData, &dstDataLen )) == NULL ) {
        fprintf(stderr, "[DEC] Error: output could not be opened (%d)\n\n", errno);
        exit(1);
    }*/

    //decode( argc, argv, f_input, f_stream );
    decode( argc, argv, srcData, dstData, 10 );

    //fclose(f_input);
    //fclose(f_stream);
    (*jenv)->ReleaseByteArrayElements(jenv, javaBytes, srcData, 0);

    for (i = 0; i < argc; i++) {
        (*jenv)->ReleaseStringUTFChars(jenv, textArray[i], argv[i]);
    }

    jbyteArray result;
    //if (dstData != NULL) {
        //printf("decode suc %zu\n", dstDataLen);
        result = cstr2jbyteArray( jenv, dstData, dstDataLen );
    //} else {
        //printf("decode fail\n");
        //return NULL;
    //}

    return result;
}

////////////////////////////////////////////////////////////////////////////////

static jbyte* jbyteArray2cstr(JNIEnv *jenv, jbyteArray javaBytes) {
    return (*jenv)->GetByteArrayElements( jenv, javaBytes, NULL );
}

////////////////////////////////////////////////////////////////////////////////

static jbyteArray cstr2jbyteArray(JNIEnv *jenv, const char *nativeStr, int dstDataLen) {
    jbyteArray javaBytes;
    javaBytes = (*jenv)->NewByteArray(jenv, dstDataLen);
    (*jenv)->SetByteArrayRegion(jenv, javaBytes, 0, dstDataLen, (jbyte*) nativeStr);
    return javaBytes;
}
