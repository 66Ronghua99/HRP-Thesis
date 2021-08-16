#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <unistd.h>
#include <malloc.h>
#include <stdbool.h>
#include <fcntl.h>

#define LOG_TAG "DetectMagiskNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static bool isLibLoaded = false;
static inline bool isMountPathDetected();
static bool isSuPathDetected();

static char *blacklistedMountPaths[] = {
        "/sbin/.magisk/",
        "/sbin/.core/mirror",
        "/sbin/.core/img",
        "/sbin/.core/db-0/magisk.db"
};

static const char *suPaths[] = {
        "/data/local/su",
        "/data/local/bin/su",
        "/data/local/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/system/bin/su",
        "/system/bin/.ext/su",
        "/system/bin/failsafe/su",
        "/system/sd/xbin/su",
        "/system/usr/we-need-root/su",
        "/system/xbin/su",
        "/cache/su",
        "/data/su",
        "/dev/su"
};

JNIEXPORT void JNICALL
Java_com_ronghua_selfcheck_Native_isLibLoaded(JNIEnv *env, jclass clazz) {
    isLibLoaded = true;
}


JNIEXPORT jboolean Java_com_ronghua_selfcheck_Native_detectMagiskNative(
        JNIEnv* env, jclass clazz) {
    LOGI("Hello from Native C code");
    bool bRet = false;
    bRet = isMountPathDetected();
    if(bRet)
        return JNI_TRUE;
    else
        return JNI_FALSE;


}

__attribute__((always_inline))
static inline bool isMountPathDetected(){
    bool bRet = false;
    int len = sizeof(blacklistedMountPaths)/ sizeof(blacklistedMountPaths[0]);
    FILE *fp = fopen("/proc/self/mounts", "r");

    if(fp == NULL)
        goto exit;

    fseek(fp, 0, SEEK_END);
    long size = ftell(fp); //calculate the size of mounts file
    LOGI("Opening Mount file size: %ld", size);

    /* For some reason size comes as zero */
    if (size == 0)
        size = 20000;  /*This will differ for different devices */

    char *buffer = malloc(sizeof(char) * size);
    if (buffer == NULL)
        goto exit;

    fseek(fp, 0, SEEK_SET);
    fread(buffer, sizeof(char), size, fp);

    for(int i=0; i<len; i++){
        LOGI("Checking Mount Path  :%s", blacklistedMountPaths[i]);
        char* index = strstr(buffer, blacklistedMountPaths[i]);
        if (index != NULL){
            LOGI("Found Mount Path  :%s", blacklistedMountPaths[i]);
            bRet = true;
            break;
        }
    }
    exit:
    if (buffer != NULL)
        free(buffer);
    if (fp != NULL)
        fclose(fp);

    return bRet;
}


static inline bool isSuPathDetected(){
    int len = sizeof(suPaths) / sizeof(suPaths[0]);

    bool bRet = false;
    for (int i = 0; i < len; i++) {
        LOGI("Checking SU Path  :%s", suPaths[i]);
        if (open(suPaths[i], O_RDONLY) >= 0) {
            LOGI("Found SU Path :%s", suPaths[i]);
            bRet = true;
            break;
        }
        if (0 == access(suPaths[i], F_OK)) {
            LOGI("Found SU Path :%s", suPaths[i]);
            bRet = true;
            break;
        }
    }

    return bRet;
}

JNIEXPORT jboolean JNICALL
Java_com_ronghua_selfcheck_Native_detectRootNative(JNIEnv *env, jclass clazz) {
    bool bRet = isSuPathDetected();
    if(bRet)
        return JNI_TRUE;
    else
        return JNI_FALSE;
}