#ifndef __ENCODER_H__
#define __ENCODER_H__

#include <stdlib.h>
#include <stdio.h>
#include <wchar.h>
#include <string.h>
#include <time.h>
#include <assert.h>
#include <sys/stat.h>
#include "options.h"
#include "cnst.h"
#include "prot.h"
#include "rom_com.h"
#include "g192.h"

//void encode( int argc, char** argv, FILE *f_input, FILE *f_stream );
void encode( int argc, char** argv, char *f_input, char *f_stream, int data_count );

#endif
