#include <jni.h>
#include <string>
#include <string.h>
#include <android/log.h>

static int verifySign(JNIEnv *env);

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    if (verifySign(env) == JNI_OK) {
        return JNI_VERSION_1_6;
    }
    return JNI_ERR;
}

static jobject getApplication(JNIEnv *env) {
    jobject application = NULL;
    jclass activity_thread_clz = env->FindClass("android/app/ActivityThread");
    if (activity_thread_clz != NULL) {
        jmethodID currentApplication = env->GetStaticMethodID(
                activity_thread_clz, "currentApplication", "()Landroid/app/Application;");
        if (currentApplication != NULL) {
            application = env->CallStaticObjectMethod(activity_thread_clz, currentApplication);
        }
        env->DeleteLocalRef(activity_thread_clz);
    }
    return application;
}

static const char *DEBUG_PACKAGE_NAME = "com.dabenxiang.mimi";

//Change this if debug on different computer(using different debug keystore)
/*
 * Timber.d("key")
 * val pm = App.applicationContext().packageManager
 * val info = pm.getPackageInfo(App.applicationContext().packageName, PackageManager.GET_SIGNATURES)
 * if(info.signatures != null)
 *   for(sign in info.signatures)
 *      Timber.d("sign: ${sign.toCharsString()}”)
 */
/*static const char *DEBUG_SIGN = "308202c3308201aba003020102020412f24423300d06092a864886f70d01010b050030123110300e06035504031307616e64726f6964301e170d313930343032303"
                                "7353534315a170d3434303332363037353534315a30123110300e06035504031307616e64726f696430820122300d06092a864886f70d01010105000382010f0030"
                                "82010a028201010081f4353ae00fe5dbd05992cc470b2115a091fb662a937a525b49cd3f4dc630f233930c617b6507499f2d31e5033bc38bc337bfd177dc9721da7"
                                "295db610394675ff5bc681010a417e0111de956c2bea62e0d61f14dd10d199113dabade9beb9ec4870cb02db643ba3328cebab603532d5710e5388dd55c9cacc0f0"
                                "89267d461141700bdb82886d9af3f11876168377465ccb0643491892e866a677ba086f71f196c81aa4d2577ae72bd9146338f35eedfe7bef1c42a5497ccc0828f6b"
                                "5b0760426793070fd803ab1ea15ebd4871ce981295cd1211baf66646588d833141708ba240f6e6f21e890a3c1f1961b72839a57e866fc7423177a3313f66bbba650"
                                "36af0203010001a321301f301d0603551d0e0416041494cf3ba0c1a4e118b4c741ed21ada8f7153df82d300d06092a864886f70d01010b050003820101006e70681"
                                "727ca4559b17703c5d7bb8017132c89a29c03f283f85c43e894da1530fe2b710a272fd1c32f624a7951a12c282d9e5f4eb77ab6d42a924d49e820fd35923b1a1131"
                                "29dc8c86180b30edc59534d8fd58daa339e354fa79e61606fe0db09996cc6cd18e1cc3dfa6cf3cb52a2ed4d06bdaa9e3e5d9950ebd772701f3d5a4cbcb49e379039"
                                "06f90c9b4e24a4611bfd938ab3ea1d9d70ae1926be562c9a3b08065d8c36a37facd184539824492f523bb4c89adf14629b0d522435743ce0906f8cf2ab86e5d105b"
                                "02c377d7172525705519ffcfaea56d17ef215b19b28cca8c8294b75435b439748b31df5b8b3034e1461a20ad8efb38298025dcf195efc04d";
 */
static const char *DEBUG_SIGN =  "25afb4312ab7ad4bf9d2cb847d8ce9cc7dff890d23db319312eff3e627a1ebdf689fe901fbddb77b998c11e708721ef966d5a7efda1043ddc83a6bda1161e0cccc2"
                                 "72dd9dc37be6f4fc7539a83639ea8424045b8e08ec448f61648050be8578adaa2a83b61cbbd9ac66e787f0a2f74b312516e0e77507781c8a5b44d548dd1b3903258"
                                 "d35d24944918149e8d6697a55df160c042b98ebe68629e7f6ac8dcaa59f185b8ae7a7666a073f1866072ab68a733ff3c51246e844d6654d4733851a4205f1e9f211"
                                 "04bbf99b61ee2fde3c1497ae3cd2f61aed22e2df1801a5ef46ab758dd3dad230a36170b66588376f94e1636a7b746c7621cb204d759913d333f794d03981504f83e"
                                 "ab3cdf6a2c1ef1cc5bf5c2d682b6c7ffbecd815e8b9e6da613536f33de44174f20a5e033755d4706ab0f3af1171b369358b24338024c6c9a924d8956e8571d65ecf"
                                 "96b31e3c1d0dda2709a3e15ac6168ec3fefbde022d4bf3ee28fa77678a972912e3fe509644b092e035b440bf140e30c4a359fe1c886179022ae687454ba5cc43322"
                                 "e506798764fee087653e362311264f403b677f1becf9fdc0dc78fafd8e0b73b05b8000de09d35a103c32aecd5225f84660ff3ae6f4ecf6fd723d636568dfcf138f1"
                                 "9e79952b1a5d23c12d4a68dfc2a129e676c9a99de19cf80888f9c2aa656ac78cf703ef585b1a6ce7e344a1a94fc8262775651c1f32359fadaeefcbcc99c7965243f"
                                 "4043edb21fde9310425dddf6f8e27d8248837ba54b776719d3eeac69212041a700e8daac8f0402b70cb0c89e14b493b031cca33af668ad1778ef768a3ea17eb0eb4"
                                 "84af5862efc6594136867416a90665597fcd70dec3afdbd791c47f42c55fc46298e46fac88fa51263fc3a2f4029efa7122d25552ce4c80991ea421f3b12755bec8c"
                                 "5bdd891b0ed1158be83441a1461abe792fe7c495ab92f1e6a6483e90e90fafa04ceec088828e40bbd130df105987b6da2c3be3f84512454bd792ac22b6f1f7138fe"
                                 "7869fe3ff56d70d62c721c482a10acddba892faa15bae13e54c22a2f5c9d4be80ddb3d704ab53fec3a6a8ac50595cbdd998fbafa151ab079b8fd0d8f6199809fd51"
                                 "3afad0929f6ecdddf5831a93d87091ceb232c0f95dc3290a3537bcfd06f5df47683f98f548c3db4f09e513c7347938049ec950bd109af65415a0e90687d1e7610fd"
                                 "e369d7084094a6650ad76677ff3ca8e2e41f4cf8b5555cf19174e7ae2e302381d52026dbae5c21b145a09b3c190ab74dde72711edd4c53cf19ca63125db79d4df35"
                                 "28600d28826873ca0bad29fa5167b7ecaefa88cec22852d5215526627d0eeac993fadd98d71bbed46c1e3bc0e1a82133525e947f8f2efc6ec74885e7684d26ba5e9"
                                 "12ec178f5ff935c6c8602f6589d0b0caa0ab5cb9306493af007aacfd313f083c21b825ba10cc49e9fac7dc06f9c4492706d67be9a8071861bff507773eea7a93343"
                                 "707208c438d043972dccfddd019f086b4d22a856de5f69e59b030499bd244a830f84933c94acc740b38c94686ee767d4e16449e19e67f2d94ce0604c86b9f64610f"
                                 "c51409f525937036bea517dccf7bbd5b0cda17712e91f7f652d18e5f81a1c1e01417e51e4a6d203c115b7592297e3b5c53820b928177a8f0533160b42b667e302f9"
                                 "1a08a4b640efb2944e53355188f788bd02ad3d812ef5cbecae68171ea633a22fd577ec808bc66da8c198ddfc7691380f55995b6bcda57bb1336358ef7dff2ffd542"
                                 "8587f4d91756fe24880267b5b0a61c7107b9c35b1372c98fbd78b97d80acdee4d2ca8f61a065b24d0a53d946af880fac81427fc1f50ed01c23f2e71047ece0ac4f4"
                                 "8307769010f30d32176f4fcb146ca01e365c41e8d50f30d52117f3919fc2d718f90412138186b0dc88198357e72789e7d51c463dce3874f955fbc3b37a8a0afab2b"
                                 "d1b49c3579e9f5f118bbb88f4c577155f0c754fb342e13e2b631ed4d38c9b2bcd4e1a25529dd1b12213ec17b8089fe975";

/*static const char *RELEASE_SIGN = "308202c5308201ada003020102020418d32e1d300d06092a864886f70d01010b050030133111300f060355040a130873696c6b726f6465301e170d313930333232"
                                  "3039353533395a170d3434303331353039353533395a30133111300f060355040a130873696c6b726f646530820122300d06092a864886f70d0101010500038201"
                                  "0f003082010a02820101008939cd8f22dafd387990933d624e36ff54a5d86daed54536312eeb4167633ac4cee9d08e85635ca99eb56c742c431e494e2253290b10"
                                  "6ce4f72e9a9632b79da74ddd1e55588fac66ee72ecc43ca525b36802804b4697527b9ed886821169ae54a2fbc1126879bc26ff5dea7a741f298df868189052ee1e"
                                  "523f8ae48db409cb958b203a0e8e5d188ebfc54c43aa980bfe6a7a33b4982236a59800654a422fb5cf87af8d37fadaedf74f4054a9141194aaad4c73cd19e33ba4"
                                  "fb64d19b4926b73dbae9cdafe19f33bd7389e348df4f3ba6aebe9e712309c033d029c7cc4b221b9ccc8fb38ea09a32fb3c49b9a401ff7d4bafcb1a222776f61fe2"
                                  "0643a2311691190203010001a321301f301d0603551d0e04160414fdced13e44fd99531c661956518d06b6878f6a0c300d06092a864886f70d01010b0500038201"
                                  "01003b433f014274fa279aa3cbcd761c5be60557693209becb778e45697d645be23c210dd9b70da7c0d5e38716356b20b7fc23890d3bc417a0d7f5ae6401de7a99"
                                  "b576554a5d4ba49851d213710d0e2b99ff8d8aa72253adb33399535edf8fb40e4f32dc1a74ae0cddc8ddbf8b8aa04eed9157bb76be825e4fad9ea070f6bcfc8c39"
                                  "0e16bc7b3fbb1fbe1a2e39ab36814be850fb23b1b5d4626a79cdd82cd89b5a8de0369ea3d32e877393d958b71f765874a3611ecc75d330deebb6440f93468b9321"
                                  "c6f2320c957a01a1e84893ea50ac3bf351029101eead2931a2d6b5b52248a1f933f06cc542aa81e374b4a9b69f1b832ebde37c27f60d813e3f3209c3d3932b";
 */
static const char *RELEASE_SIGN = "be59241d4601c2b1e92199220c86c587c5ccde1cad70d2fc2b3877c1ac5c7fadcb23dc75ef9ff1331dffe579e78c4045953e803c06ed755f85aa8992641ef01a0ba"
                                  "4141a6bf80f1723609323c702acf1248f74f8ce385b65c0cde65dc938e790ff95b92107deb35143fda76c439682f9c03aa7ed6b20e57fe4a1b8fb9e500dfa28bb52"
                                  "01508289caa333676b3fc5c25375b3e369a16c040be8309ef47b29b0ae284aee33d045ae07f262dfd4a54469d67fd3f4b585b4cd301f382efcc2f8930d14883628c"
                                  "03520b8032486195f61c013d22791fcdf3ef7f83eda5890357bae135e2ab13ce6f0a7786532f96e9709ca4eac7543db495875b5c4e0003bcde5157704423ae39974"
                                  "ab1b2c5a0477dbc45275cca31f2ab03bc0a42e9f13978ac2f0909c138363c77f573744bc2ffef3a3eff4134f8257bf694666d46cae9d52a8c9a6d32baf8c1208189"
                                  "375b8e531129551bf8ed5d3ff7b479add4f1052b551a22ebdf6938b0732af6113cc8960ae741d59458603c8104afe1b74e2ed7b699a5708caf66b0ad67522b588fb"
                                  "c9f6ee575950d977e7089ebc3f412e899ea54174e8e028aa2e3b65da963e5798871725bfff1be32429ee4c8f84e38d6b9ec1e92fd679cb9546e279f76b0872ce43f"
                                  "7f7707bcf41c98fa085d16e165cdf5c47b14214668c9b2dfd371dbc9a8d2aabeacf4207a892f549dbe53fe751abc42fa82e69b92617f30da5a4aa7d806e9dcbba1a"
                                  "97baa9a1462143b5bcc73d521b1889bee150964b0a2e546e27241b45b40a5f488c1cb38a80d5ca5d39d4e04f24076ff3927a6d21bfb435f5e57de3c3e371fa4c7a6"
                                  "8be8d3cd002de3b3c1fd8a8ea214245fe460eda4ec7f620f44e801082518345dc3dd0fd3ca6d4522fe351ea65ffe93b256e9027b2884945c6da0e9c72dfd6449eeb"
                                  "73dbfc19977dcb14197fd419e1b8b41e074170e5d4e9989696b536a388be0711458bb7e9dfdd9c57afd81ed81a6b00fab294ac02d0375913215a2d54c45b6a8eb4a"
                                  "ad03832cc301b6c7253e7a13d2f6e1e40d6b6b17607ba3672ddc64b534d38f8eb7591b6e1b5f41373ce2bc95f137ec14b2ac190f0aaece545f631a8f3d6e90d35cd"
                                  "68a5611133a05e3fb87e5fef77a1064be6909c0c288f8acaf34c0a16d5de1ab9b7105c2a054d099fedf71b483a12bc6d903ec5bfd6c3ba1d4c336565a049d950621"
                                  "93f96be211460133a771645292b67eb0bb48b6b7b23ce6df9f9dbdec334ac47207aa2b3f71164923e1fdf17bb7102876b4d48a3a29988c97845e99dd94ce74b7e6a"
                                  "d0a0f78907eef9ac9c93048f65ac08d0aad84d39308456d74d330281d63be047a95d5e14bc33406e9f22bf1b934d53fca9335e855afe252ab12cad8720518ae94b3"
                                  "40a17698dcbed43417abf4b8ea062eb12ad67a8012ebe282fa989a56c6aaabd4cd185825bb423489f097fc04fb9a19d8735becda694bb2316c8552a61a632822072"
                                  "07b02dd60f8377e7d5e0a8873a6bccfbe20e31bd0e0e41e997fd6b3efd92ae33c1b890d59a9fc86c428be5dc88f0c4dbae84af04271fe4168858a8a2ab6b37f32d6"
                                  "eb547892ca93d94eadc40604f74e3f20ad0c911dc097fababdfdaa1fba76ba5097ad34e20fc5098cedbe9f138bfabe83c83d2aa8a16f5901274fd1c80bad1f9b24c"
                                  "4baf3576b491afc11bf3437ba8757255c5fc982183db982e88cafd21f27fbbbfb552a6875e334b376779fcff956ae5f59a04f123df24eae1af00957ed591e525f85"
                                  "1cb1c8a614b132eeb97dbf91e43038178b837974afb6230809e7e40ba68f4663448ac196cc10ccc8fe18737cc71e9a69c68b1df47dc5039313bbd7d9babad26cff5"
                                  "d1f548e892cffd3b322d67d788eb8ee77033418d0e334f6f5f5fa558c74cbe4c5e66f825960ce1c829e2e7b99f6f24a734b7d66af8e673b95ebbfc95f7fe018b1a0"
                                  "277c5994f5b3d0d2176bbf696d73ddcfeda44b8ced0c1109df1c387e841e3d715b3ea39fb234efaf379ea67e20f15de9ea1277f69448b276e5fd12e8a8f546165";


extern "C" JNIEXPORT jstring JNICALL
Java_com_dabenxiang_plugin_view_login_LoginViewModel_stringFromJNI(JNIEnv *env, jobject) {
    return env->NewStringUTF("Hello from C++");
}

#ifndef _Included_com_wobiancao_ndkjnidemo_ndk_JniUtils
#define _Included_com_wobiancao_ndkjnidemo_ndk_JniUtils
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_wobiancao_ndkjnidemo_ndk_JniUtils
 * Method:    getStringFormC
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_wobiancao_ndkjnidemo_ndk_JniUtils_getStringFormC
        (JNIEnv *, jclass);

/*
 * Class:     com_wobiancao_ndkjnidemo_ndk_JniUtils
 * Method:    getKeyValue
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_wobiancao_ndkjnidemo_ndk_JniUtils_getKeyValue
        (JNIEnv *, jclass);

/*
 * Class:     com_wobiancao_ndkjnidemo_ndk_JniUtils
 * Method:    getIv
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_wobiancao_ndkjnidemo_ndk_JniUtils_getIv
        (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif


/////////////////////////////////////////////////////////////////////////////////
#include <sstream>
extern "C"{
#include "aes.h"
}
//把字符串转成十六进制字符串
std::string char2hex(std::string s)
{
    std::string ret;
    for (unsigned i = 0; i != s.size(); ++i)
    {
        char hex[5];
        sprintf(hex, "%.2x", (unsigned char)s[i]);
        ret += hex;
    }
    return ret;
}
//把十六进制字符串转成字符串
std::string hex2char(std::string s)
{
    std::string ret;
    int length = (int) s.length();
    for (int i = 0; i <length ; i+=2) {
        std::string buf = "0x"+s.substr(i,2);
        unsigned int value;
        sscanf(buf.c_str(), "%x", &value);
        ret += ((char)value);
    }
    return ret;
}
int hexCharToInt(char c){
    if(c >= '0' && c <= '9') return (c - '0');
    if(c >= 'A' && c <= 'F') return (c - 'A'+10);
    if(c >= 'a' && c <= 'f') return (c - 'a'+10);
    return 0;
}

//十六进制字符串转成十六进制数组
char * hexstringToBytes(std::string s){
    int sz = (int) s.length();
    char  *ret = new char[sz/2];
    for (int i = 0; i < sz; i+=2) {
        ret[i/2] = (char)((hexCharToInt(s.at(i)) << 4)|hexCharToInt(s.at(i+1)));
    }
    return ret;
}
//十六进制数组转成十六进制字符串
std::string bytestohexstring(char *bytes,int bytelength){
    std::string str("");
    std::string str2("0123456789abcdef");
    for (int i = 0; i < bytelength; ++i) {
        int b;
        b = 0x0f&(bytes[i]>>4);
        char s1 = str2.at(b);
        str.append(1,str2.at(b));
        b = 0x0f&bytes[i];
        str.append(1,str2.at(b));
//        char s2 = str2.at(b);
    }
    return str;
}

//加密
std::string EncodeEcbAES(const unsigned char *master_key,std::string data){
    AES_KEY key;
    AES_set_encrypt_key(master_key, 128, &key);

    std::string data_bak = data.c_str();
    unsigned int data_length = (unsigned int) data_bak.length();

    std::string encryhex;
    for(unsigned int i = 0; i < data_length/AES_BLOCK_SIZE; i++)
    {
        std::string str16 = data_bak.substr(i*AES_BLOCK_SIZE, AES_BLOCK_SIZE);
        unsigned char out[AES_BLOCK_SIZE];
        memset(out, 0, AES_BLOCK_SIZE);
        AES_ecb_encrypt((const unsigned char *) str16.c_str(), out, &key, AES_ENCRYPT);
        encryhex += bytestohexstring((char *) out, AES_BLOCK_SIZE);
    }
    return encryhex;

}
//解密
std::string DecodeEcbAES(const unsigned char *master_key,std::string data){
    AES_KEY key;
    AES_set_decrypt_key(master_key, 128, &key);

    std::string ret;
    for(unsigned int i = 0; i < data.length()/(AES_BLOCK_SIZE*2); i++)
    {
        std::string str16 = data.substr(i*AES_BLOCK_SIZE*2, AES_BLOCK_SIZE*2);
        unsigned char out[AES_BLOCK_SIZE];
        memset(out, 0, AES_BLOCK_SIZE);
        char *buf = hexstringToBytes(str16);
        AES_ecb_encrypt((const unsigned char *)buf , out, &key, AES_DECRYPT);
        delete(buf);
        ret += hex2char(bytestohexstring((char *) out, AES_BLOCK_SIZE));
    }
    return ret;
}



//加密
std::string EncodeAES(const unsigned char *master_key,std::string data,const unsigned char *iv){
    AES_KEY key;
    AES_set_encrypt_key(master_key, 128, &key);

    unsigned char ivc[AES_BLOCK_SIZE];

    std::string data_bak = data.c_str();
    unsigned int data_length = (unsigned int) data_bak.length();
    int padding = 0;
    if (data_bak.length() % AES_BLOCK_SIZE >= 0)
    {
        padding = (int) (AES_BLOCK_SIZE - data_bak.length() % AES_BLOCK_SIZE);
    }
    data_length += padding;
    while (padding > 0)
    {
        data_bak += '\0';
        padding--;
    }

    memcpy( ivc, iv, AES_BLOCK_SIZE*sizeof(char));
    std::string encryhex;
    for(unsigned int i = 0; i < data_length/AES_BLOCK_SIZE; i++)
    {
        std::string str16 = data_bak.substr(i*AES_BLOCK_SIZE, AES_BLOCK_SIZE);
        unsigned char out[AES_BLOCK_SIZE];
        memset(out, 0, AES_BLOCK_SIZE);
        AES_cbc_encrypt((const unsigned char *) str16.c_str(), out, 16, &key, ivc, AES_ENCRYPT);
        encryhex += bytestohexstring((char *) out, AES_BLOCK_SIZE);
    }
    return encryhex;

}
//解密
std::string DecodeAES(const unsigned char *master_key,std::string data,const unsigned char *iv){
    AES_KEY key;
    AES_set_decrypt_key(master_key, 128, &key);

    unsigned char ivc[AES_BLOCK_SIZE];
    memcpy( ivc, iv, AES_BLOCK_SIZE*sizeof(char));
    std::string ret;
    for(unsigned int i = 0; i < data.length()/(AES_BLOCK_SIZE*2); i++)
    {
        std::string str16 = data.substr(i*AES_BLOCK_SIZE*2, AES_BLOCK_SIZE*2);
        unsigned char out[AES_BLOCK_SIZE];
        memset(out, 0, AES_BLOCK_SIZE);
        char *buf = hexstringToBytes(str16);
        AES_cbc_encrypt((const unsigned char *)buf , out, AES_BLOCK_SIZE, &key, ivc, AES_DECRYPT);
        delete(buf);
        ret += hex2char(bytestohexstring((char *) out, AES_BLOCK_SIZE));
    }
    return ret;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_dabenxiang_mimi_widget_utility_CryptUtils_cEcbEncrypt(JNIEnv *env, jobject thiz, jstring str_) {
    const char *str = env->GetStringUTFChars(str_, 0);

    const unsigned char *master_key = (const unsigned char *) "1234567890123456";

    std::string h = EncodeEcbAES(master_key, str);

    env->ReleaseStringUTFChars(str_, str);
    return env->NewStringUTF(h.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_dabenxiang_mimi_widget_utility_CryptUtils_cEcbDecrypt(JNIEnv *env, jobject thiz, jstring str_) {
    const char *str = env->GetStringUTFChars(str_, 0);

    const unsigned char *master_key = (const unsigned char *) "1234567890123456";


    std::string s = DecodeEcbAES(master_key,str);
    env->ReleaseStringUTFChars(str_, str);

    return env->NewStringUTF(s.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_dabenxiang_mimi_widget_utility_CryptUtils_cEncrypt(JNIEnv *env, jobject thiz, jstring str_) {
    const char *str = env->GetStringUTFChars(str_, 0);

    const unsigned char *master_key = (const unsigned char *) "1234567890abcdef";
    const unsigned char *iv = (const unsigned char *) "90abcdef12345678";

    std::string h = EncodeAES(master_key, str, iv);

    env->ReleaseStringUTFChars(str_, str);
    return env->NewStringUTF(h.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_dabenxiang_mimi_widget_utility_CryptUtils_cDecrypt(JNIEnv *env, jobject thiz, jstring str_) {
    const char *str = env->GetStringUTFChars(str_, 0);

    const unsigned char *master_key = (const unsigned char *) "1234567890abcdef";

    const unsigned char *iv = (const unsigned char *) "90abcdef12345678";

    std::string s = DecodeAES(master_key,str,iv);
    env->ReleaseStringUTFChars(str_, str);

    return env->NewStringUTF(s.c_str());
}

bool isVerify =false;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_dabenxiang_mimi_widget_utility_CryptUtils_cIsVerify(JNIEnv *env, jobject thiz) {
    return isVerify;
}

static int verifySign(JNIEnv *env) {
    // Application object
    jobject application = getApplication(env);
    if (application == NULL) {
        return JNI_ERR;
    }
    // Context(ContextWrapper) class
    jclass context_clz = env->GetObjectClass(application);
    // getPackageManager()
    jmethodID getPackageManager = env->GetMethodID(context_clz, "getPackageManager",
                                                   "()Landroid/content/pm/PackageManager;");
    // android.content.pm.PackageManager object
    jobject package_manager = env->CallObjectMethod(application, getPackageManager);
    // PackageManager class
    jclass package_manager_clz = env->GetObjectClass(package_manager);
    // getPackageInfo(String, int)
    jmethodID getPackageInfo = env->GetMethodID(package_manager_clz, "getPackageInfo",
                                                "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    // context.getPackageName()
    jmethodID getPackageName = env->GetMethodID(context_clz, "getPackageName",
                                                "()Ljava/lang/String;");
    // call getPackageName() and cast from jobject to jstring
    jstring package_name = (jstring) (env->CallObjectMethod(application, getPackageName));
    // PackageInfo object
    jobject package_info = env->CallObjectMethod(package_manager, getPackageInfo, package_name, 64);
    // class PackageInfo
    jclass package_info_clz = env->GetObjectClass(package_info);
    // field signatures
    jfieldID signatures_field = env->GetFieldID(package_info_clz, "signatures",
                                                "[Landroid/content/pm/Signature;");
    jobject signatures = env->GetObjectField(package_info, signatures_field);
    jobjectArray signatures_array = (jobjectArray) signatures;
    jobject signature0 = env->GetObjectArrayElement(signatures_array, 0);
    jclass signature_clz = env->GetObjectClass(signature0);

    jmethodID toCharsString = env->GetMethodID(signature_clz, "toCharsString",
                                               "()Ljava/lang/String;");
    // call toCharsString()
    jstring signature_str = (jstring) (env->CallObjectMethod(signature0, toCharsString));

    // release
    env->DeleteLocalRef(application);
    env->DeleteLocalRef(context_clz);
    env->DeleteLocalRef(package_manager);
    env->DeleteLocalRef(package_manager_clz);
    env->DeleteLocalRef(package_info);
    env->DeleteLocalRef(package_info_clz);
    env->DeleteLocalRef(signatures);
    env->DeleteLocalRef(signature0);
    env->DeleteLocalRef(signature_clz);

    const char *sign = env->GetStringUTFChars(signature_str, NULL);
    if (sign == NULL) {
        return JNI_ERR;
    }

    //Verify debug package name
    const char *packageName = env->GetStringUTFChars(package_name, NULL);

    const unsigned char *master_key = (const unsigned char *) "1234567890abcdef";

    const unsigned char *iv = (const unsigned char *) "90abcdef12345678";

    int result;

#ifdef DEBUG
    std::string str(DEBUG_SIGN);
    std::string decryptKey = DecodeAES(master_key, str,iv);
    const char *decryptKeyStr = decryptKey.c_str();
    result = strcmp(sign, decryptKeyStr);
    __android_log_print(ANDROID_LOG_DEBUG, "CMAKE", "cmake debug\n");
#else
    std::string str(RELEASE_SIGN);
    std::string decryptKey = DecodeAES(master_key, str,iv);
    const char *decryptKeyStr = decryptKey.c_str();
    result = strcmp(sign, decryptKeyStr);
        __android_log_print(ANDROID_LOG_DEBUG, "CMAKE", "cmake release\n");
#endif

    // Release string memory
    env->ReleaseStringUTFChars(package_name, packageName);
    env->DeleteLocalRef(package_name);
    env->ReleaseStringUTFChars(signature_str, sign);
    env->DeleteLocalRef(signature_str);
    if (result == 0) { // 0 = identical
        isVerify =true;
        return JNI_OK;
    }else{
        isVerify =false;
    }

    return JNI_OK;
}