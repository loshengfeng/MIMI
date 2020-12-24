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

static const char *DEBUG_PACKAGE_NAME = "com.dabenxiang.plugin";

//Change this if debug on different computer(using different debug keystore)
/*
 * Timber.d("key")
 * val pm = App.applicationContext().packageManager
 * val info = pm.getPackageInfo(App.applicationContext().packageName, PackageManager.GET_SIGNATURES)
 * if(info.signatures != null)
 *   for(sign in info.signatures)
 *      Timber.d("sign: ${sign.toCharsString()}”)
 */
static const char *DEBUG_SIGN = "308202e9308201d1a00302010202041f1a36c0300d06092a864886f70d01010b050030253111300f060355040b130873696c6b726f64653110300e060355040313"
                                "07616e64726f6964301e170d3139303731393130313435335a170d3434303731323130313435335a30253111300f060355040b130873696c6b726f64653110300e"
                                "06035504031307616e64726f696430820122300d06092a864886f70d01010105000382010f003082010a0282010100bc42435e13e70b7301214e89e92941cbf04e"
                                "20c5b58090c696314439d47c4fadd04f25286357d148ff604dd8bdf12af691af7c2d978828613041342efe08929f5b77156546db0b42ed909bd734c020a335ce9d"
                                "b2e1d49ad3ffd7d4611a03bc92bb99504511ec782243600d1092ffb8908598b5e6e9867a3cf1d58b1680699bd4fa9dfd22fa47810f6909cc71799baa6a66d18239"
                                "6274c78594c7db4c84351beb4ad41c592cc2ed7a69e76af28d680d3829b7905650a1fb6b42afc3676a191df348ff83ccf8689a6a524a6facab8f468926dae33cd0"
                                "6d2d14d2dc73c87109677167015d217d25575baa6bc89d46f7bbb3e646a753ba75917e0467de839ac988f50203010001a321301f301d0603551d0e04160414f267"
                                "eee68cf02382f770132083d541961eb0209e300d06092a864886f70d01010b0500038201010081117b591d6162deb44d22790003870415ddeb1df8d609a4a89ab1"
                                "9563768486ad708a35553e4f354f11d5405418b7e4f45cbdb77404a2d894e8e368b76f01d91d681aca17ae3e75271970bd671a98f3338ed7328eddd6de4cd355ca"
                                "f3fec4a9f472d3725af42d377347032adcb33d42e845d67a8b1adbf4541ba67da93399fb22f7554482e2363d03f9a003fe46ca8be6bc501f9910aa3ac98d42dd65"
                                "463c7d96c881a737176758b45ea68b2a6b9274f0207510853ad788d8efedace61c5443aeb251afdb019424ca4a29e5da67b06c7d08f65b75abb9db1d430ff27568"
                                "890efc209c107e1f8429d84a4f06c4411a1b4cec7570bb05d243838d37c56584686f";

static const char *RELEASE_SIGN = "308202e9308201d1a00302010202045bb5ad56300d06092a864886f70d01010b050030253111300f060355040b130873696c6b726f64653110300e0603550403"
                                  "1307616e64726f6964301e170d3139303731393130313630385a170d3434303731323130313630385a30253111300f060355040b130873696c6b726f64653110"
                                  "300e06035504031307616e64726f696430820122300d06092a864886f70d01010105000382010f003082010a0282010100a8ef20451a278f4e63c6985913f2a7"
                                  "47fedecd4025830559fa5d1038380712d0f1f3a0dd504e0be910ee1b7e72e947cce4d62cb6b09b55cb256216184d5351ee71f7a9c0b5c288f6f70d5d70f948ed"
                                  "a96f11adad367722daac6ddcb48dff1159bc65ea63117951581dd883e6f714b3d6fa1e4249bbfdb0df660ecab6c4dd48a0d7ac4f7942caabfa7873bb74b7aa7a"
                                  "c30e1d59ece22d91b6356c9deb2a90febdc802bf6530b57e7240d578bd75873965eec1e4d3eeb62d886202d1d542f455f766d9536fe3163219f002256ce9ece9"
                                  "75ddfff82efbd43a22ad7acee119edaae6ce80398ba5de027a223838b7fafe79e534a763fec98a350aeccf9e2ee080089d0203010001a321301f301d0603551d"
                                  "0e04160414efd7f7dea2e53e5ce3ead733b70f412817bb1a62300d06092a864886f70d01010b050003820101006994d01f6e0c21836aa3407a663dab3bae178b"
                                  "4ddfb6ed65c3362316629d172e128a65c60644cfe920e627ab2ed066eeadb71122569e33944b0107947702e5274cf29e37c85ea9dbcbb869fcd9d37d1d914fb0"
                                  "78d9d86cd86d02279dd90c5338b3df0b6ce95ccc72fae70d7a0927deabb9293563bd42b795997425184a8f79ee8cf7178023a14ef80f7ed4582bfb100a3080fb"
                                  "8254a3c75060b04ab195cb07e083da7e09e8efe6ce1a6201e5c0cbf7ecb18143e14ed670a550fdfd11b0e91202151f8d704ddeb4395ae4e92023707720c5461b"
                                  "3548936158fad597168a5939f56ea87ee38237e7343bda9d8eba327aba49d822754f40d28b7f4ea67dd8a9dae4";

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
    int result;
#ifdef DEBUG
    result = strcmp(sign, DEBUG_SIGN);
    __android_log_print(ANDROID_LOG_DEBUG, "CMAKE", "cmake debug\n");
#else
    result = strcmp(sign, RELEASE_SIGN);
        __android_log_print(ANDROID_LOG_DEBUG, "CMAKE", "cmake release\n");
#endif

    // Release string memory
    env->ReleaseStringUTFChars(package_name, packageName);
    env->DeleteLocalRef(package_name);
    env->ReleaseStringUTFChars(signature_str, sign);
    env->DeleteLocalRef(signature_str);
    if (result == 0) { // 0 = identical
        return JNI_OK;
    }

    return JNI_OK;
}

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

/*
 * Class:     Java_com_wobiancao_ndkjnidemo_ndk_JniUtils
 * Method:    getStringFormC
 * Signature: ()Ljava/lang/String;
 */
//extern "C" JNIEXPORT jstring JNICALL Java_com_wobiancao_ndkjnidemo_ndk_JniUtils_getStringFormC
//        (JNIEnv *env, jobject obj){
//    return env->NewStringUTF("这里是来自c的string");
//}

//const char keyValue[] = {
//        21, 25, 21, -45, 25, 98, -55, -45, 10, 35, -45, 35,
//        26, -5, 25, -65, -78, -99, 85, 45, -5, 10, -0, 11,
//        -35, -48, -98, 65, -32, 14, -67, 25, 36, -56, -45, -5,
//        12, 15, 35, -15, 25, -14, 62, -25, 33, -45, 55, 12, -8
//};
//
//const char iv[] =  {    //16 bit
//        -33, 32, -25, 25, 35, -27, 55, -12, -15,32,
//        23, 45, -26, 32, 5,16
//};
//
//extern "C" jbyteArray Java_com_dabenxiang_plugin_widget_utility_JniUtils_getKeyValue(JNIEnv *env, jobject obj)
//{
//
//    jbyteArray kvArray = env->NewByteArray(sizeof(keyValue));
//    jbyte *bytes = env->GetByteArrayElements(kvArray,0);
//
//    int i;
//    for (i = 0; i < sizeof(keyValue);i++){
//        bytes[i] = (jbyte)keyValue[i];
//    }
//
//    env->SetByteArrayRegion(kvArray, 0, sizeof(keyValue),bytes);
//    env->ReleaseByteArrayElements(kvArray,bytes,0);
//
//    return kvArray;
//}
//
////JNIEXPORT JNICALL
//extern "C" jbyteArray Java_com_dabenxiang_plugin_widget_utility_JniUtils_getIv(JNIEnv *env, jobject obj)
//{
//
//    jbyteArray ivArray = env->NewByteArray(sizeof(iv));
//    jbyte *bytes = env->GetByteArrayElements(ivArray, 0);
//
//    int i;
//    for (i = 0; i < sizeof(iv); i++){
//        bytes[i] = (jbyte)iv[i];
//    }
//
//    env->SetByteArrayRegion(ivArray, 0, sizeof(iv), bytes);
//    env->ReleaseByteArrayElements(ivArray,bytes,0);
//
//    return ivArray;
//}

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