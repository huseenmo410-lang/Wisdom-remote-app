#include <jni.h>
#include <string>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <android/log.h>

#define LOG_TAG "SamsungRemoteNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Native socket implementation for legacy Samsung Wisdom/Cloud TVs (Port 55000)
extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_tvremote_TvNetworkService_sendLegacyCommandNative(
        JNIEnv* env,
        jobject /* this */,
        jstring tvIpStr,
        jint port,
        jbyteArray payloadBytes) {

    const char *ip = env->GetStringUTFChars(tvIpStr, nullptr);
    jbyte* buffer = env->GetByteArrayElements(payloadBytes, nullptr);
    jsize length = env->GetArrayLength(payloadBytes);

    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        LOGE("Failed to create socket");
        env->ReleaseStringUTFChars(tvIpStr, ip);
        env->ReleaseByteArrayElements(payloadBytes, buffer, JNI_ABORT);
        return JNI_FALSE;
    }

    struct sockaddr_in serverAddr{};
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(port);
    inet_pton(AF_INET, ip, &serverAddr.sin_addr);

    // 2 Second Connection Timeout
    struct timeval tv{};
    tv.tv_sec = 2;
    tv.tv_usec = 0;
    setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, (const char*)&tv, sizeof(tv));
    setsockopt(sock, SOL_SOCKET, SO_SNDTIMEO, (const char*)&tv, sizeof(tv));

    if (connect(sock, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        LOGE("Connection to TV failed at %s:%d", ip, port);
        close(sock);
        env->ReleaseStringUTFChars(tvIpStr, ip);
        env->ReleaseByteArrayElements(payloadBytes, buffer, JNI_ABORT);
        return JNI_FALSE;
    }

    ssize_t sent = send(sock, buffer, length, 0);
    LOGI("Sent %zd bytes to TV successfully", sent);

    close(sock);
    env->ReleaseStringUTFChars(tvIpStr, ip);
    env->ReleaseByteArrayElements(payloadBytes, buffer, JNI_ABORT);
    return (sent > 0) ? JNI_TRUE : JNI_FALSE;
}
