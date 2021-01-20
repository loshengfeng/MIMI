#ifndef _AES_H_
#define _AES_H_

#include <stdint.h>
#include <string.h>
#include <stdlib.h>
#include "base64.h"

#ifndef HEADER_AES_H
# define HEADER_AES_H

# include <stddef.h>

# define AES_ENCRYPT     1
# define AES_DECRYPT     0

# define AES_MAXNR 14
# define AES_BLOCK_SIZE 16

struct aes_key_st {
# ifdef AES_LONG
    unsigned long rd_key[4 * (AES_MAXNR + 1)];
# else
    unsigned int rd_key[4 * (AES_MAXNR + 1)];
# endif
    int rounds;
};

typedef struct aes_key_st AES_KEY;


int AES_set_encrypt_key(const unsigned char *userKey, const int bits, AES_KEY *key);
int AES_set_decrypt_key(const unsigned char *userKey, const int bits, AES_KEY *key);

void AES_encrypt(const unsigned char *in, unsigned char *out, const AES_KEY *key);
void AES_decrypt(const unsigned char *in, unsigned char *out, const AES_KEY *key);

void AES_ecb_encrypt(const unsigned char *in, unsigned char *out, const AES_KEY *key, 
					const int enc);

void AES_cbc_encrypt(const unsigned char *in, unsigned char *out,
                     size_t length, const AES_KEY *key,
                     unsigned char *ivec, const int enc);

#endif

// #define the macros below to 1/0 to enable/disable the mode of operation.
//
// CBC enables AES128 encryption in CBC-mode of operation and handles 0-padding.
// ECB enables the basic ECB 16-byte block algorithm. Both can be enabled simultaneously.

// The #ifndef-guard allows it to be configured before #include'ing or at compile time.
#ifndef CBC
#define CBC 1
#endif

#ifndef ECB
#define ECB 1
#endif


static const unsigned  char HEX[16]={0x10,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f};

#ifdef __cplusplus
extern "C" {
#endif

#if defined(ECB) && ECB

char* AES_ECB_PKCS7_Encrypt(const char *in, const uint8_t *key);
char* AES_ECB_PKCS7_Decrypt(const char *in, const uint8_t *key);

#endif // #if defined(ECB) && ECB


#if defined(CBC) && CBC

char *AES_CBC_PKCS7_Encrypt(const char *in, const uint8_t *key, const uint8_t *iv);
char *AES_CBC_PKCS7_Decrypt(const char *in, const uint8_t *key, const uint8_t *iv);

#endif // #if defined(CBC) && CBC


#ifdef __cplusplus
}
#endif

#endif //_AES_H_

