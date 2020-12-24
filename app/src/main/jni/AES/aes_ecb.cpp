#ifndef AES_DEBUG
# ifndef NDEBUG
#  define NDEBUG
# endif
#endif

#include <assert.h>

#include "aes.h"
#include "aes_locl.h"

void AES_ecb_encrypt(const unsigned char *in, unsigned char *out,
                     const AES_KEY *key) {

    assert(in && out && key);
    AES_encrypt(in, out, key);
}

void AES_ecb_decrypt(const unsigned char *in, unsigned char *out,
                     const AES_KEY *key) {

    assert(in && out && key);

    AES_decrypt(in, out, key);
}

// ======================== 加密 ============================
// in-->in
void AESEncodeDataIn(unsigned char *in, unsigned int len, unsigned char *userKey) {
    AES_KEY aes;
    if (AES_set_encrypt_key((unsigned char *) userKey, 128, &aes) < 0) {
        return;
    }

    unsigned char temp[AES_BLOCK_SIZE];
    int j = 0;
    for (j = 0; j < (len / AES_BLOCK_SIZE); j++) {
        AES_ecb_encrypt(in + j * AES_BLOCK_SIZE, temp, &aes);
        //memcpy((void *)in + j * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
        memcpy(in + j * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
    }
}

// in-->out
void AESEncodeDataInOut(unsigned char *in, unsigned char *out, unsigned int len,
                        unsigned char *userKey) {
    AES_KEY aes;
    if (AES_set_encrypt_key((unsigned char *) userKey, 128, &aes) < 0) {
        return;
    }

    unsigned char temp[AES_BLOCK_SIZE];
    int j = 0;
    for (j = 0; j < (len / AES_BLOCK_SIZE); j++) {
        AES_ecb_encrypt(in + j * AES_BLOCK_SIZE, temp, &aes);
        //memcpy((void *)out + j * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
        memcpy(out + j * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
    }
}

// ======================== 解密 ============================
// in-->in
void AESDecodeDataIn(unsigned char *in, unsigned int len,
                     unsigned char *userKey) {
    AES_KEY aes;
    if (AES_set_decrypt_key((unsigned char *) userKey, 128, &aes) < 0) {
        return;
    }

    unsigned char temp[AES_BLOCK_SIZE];
    int j = 0;
    for (j = 0; j < (len / AES_BLOCK_SIZE); j++) {
        AES_ecb_decrypt(in + j * AES_BLOCK_SIZE, temp, &aes);
        //memcpy((void *)in + j * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
        memcpy(in + j * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
    }
}

// in-->out
void AESDecodeDataInOut(unsigned char *in, unsigned char *out, unsigned int len,
                        unsigned char *userKey) {
    AES_KEY aes;
    if (AES_set_decrypt_key((unsigned char *) userKey, 128, &aes) < 0) {
        return;
    }

    unsigned char temp[AES_BLOCK_SIZE];
    int j = 0;
    for (j = 0; j < (len / AES_BLOCK_SIZE); j++) {
        AES_ecb_decrypt(in + j * AES_BLOCK_SIZE, temp, &aes);
        //memcpy((void *)out + j * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
        memcpy(out + j * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
    }
}


// 测试代码
void AES_ecb_test() {
    printf("AES_ecb_test\n");

    int i = 0;
    const char *AESKey = "1234567890ABCDEF";

    // 待加密字串的长度必须指定,不能依赖 strlen
    int lenData = 20;
    unsigned char *data = (unsigned char *) malloc(20);
    data[0] = 0x01;
    data[1] = 0x00;
    data[2] = 0x02;
    data[3] = 0x00;
    data[4] = 0x03;
    data[5] = 0x00;
    data[6] = 'a';
    data[7] = 'b';
    data[8] = 'c';
    data[9] = 0;
    data[10] = '1';
    data[11] = '2';
    data[12] = '3';
    data[13] = '\0';
    data[14] = 0x01;
    data[15] = 0x02;
    data[16] = 0x03;
    data[17] = 0x04;
    data[18] = 0x05;
    data[19] = 0x06;

    printf("\ndata origin = \n");
    for (i = 0; i < lenData; ++i) {
        printf("0x%x ", data[i]);
        if (0 == (i + 1) % 16)
            printf("\n");
    }
    printf("\n");

    int lenDataEncrypt = 0;
    if (lenData % AES_BLOCK_SIZE != 0) {
        lenDataEncrypt = AES_BLOCK_SIZE + lenData - (lenData % AES_BLOCK_SIZE);
    }

    // 加密
    AES_KEY aes;
    if (AES_set_encrypt_key((unsigned char *) AESKey, 128, &aes) < 0) {
        return;
    }

    unsigned char temp[AES_BLOCK_SIZE];
    for (i = 0; i < (lenDataEncrypt / 16); i++) {
        AES_ecb_encrypt(data + i * AES_BLOCK_SIZE, temp, &aes);
        memcpy(data + i * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
    }

    printf("\ndata encrypt = \n");
    for (i = 0; i < lenDataEncrypt; ++i) {
        printf("0x%x ", data[i]);
        if (0 == (i + 1) % 16)
            printf("\n");
    }
    printf("\n");

    // 解密
    if (AES_set_decrypt_key((unsigned char *) AESKey, 128, &aes) < 0) {
        return;
    }

    for (i = 0; i < lenDataEncrypt / AES_BLOCK_SIZE; i++) {
        AES_ecb_decrypt(data + i * AES_BLOCK_SIZE, temp, &aes);
        memcpy(data + i * AES_BLOCK_SIZE, temp, AES_BLOCK_SIZE);
    }

    printf("\ndata decrypt = \n");
    for (i = 0; i < lenData; ++i) {
        printf("0x%x ", data[i]);
        if (0 == (i + 1) % 16)
            printf("\n");
    }
    printf("\n");
}

