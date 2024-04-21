#include <jni.h>
#include <string>
#include <iostream>
#include <map>
#include <sstream>
#include <cstdlib>
#include <vector>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/ip.h>
#include <netinet/ip_icmp.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>

#define PACKET_SIZE 64
#define ICMP_ECHO_REQUEST 8

typedef struct httpReq
{
    std::string request_url;

    char * data;
} httpReq;


std::map<std::string, std::string> extractParams(const std::string& uri) {
    std::map<std::string, std::string> params;

    size_t pos = uri.find('?');
    if (pos != std::string::npos) {

        std::string paramString = uri.substr(pos + 1);


        std::istringstream iss(paramString);
        std::string token;
        while (std::getline(iss, token, '&')) {

            size_t equalsPos = token.find('=');
            if (equalsPos != std::string::npos) {
                std::string key = token.substr(0, equalsPos);
                std::string value = token.substr(equalsPos + 1);
                params[key] = value;
            }
        }
    }

    return params;
}



std::vector<std::string> pingRange(const std::string& startAddr, const std::string& endAddr) {
    std::vector<std::string> reachableAddresses;


    unsigned int start = inet_addr(startAddr.c_str());
    unsigned int end = inet_addr(endAddr.c_str());


    for (unsigned int i = htonl(start); i <= htonl(end); ++i) {

        struct in_addr addr;
        addr.s_addr = htonl(i);
        std::string address = std::string(inet_ntoa(addr));

        reachableAddresses.push_back(address);

    }


    return reachableAddresses;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_forearm_1curl_PingResultsFragment_pingRange(JNIEnv *env, jobject thiz,
                                                             jstring start_addr, jstring end_addr) {
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    const char* startAddrStr = env->GetStringUTFChars(start_addr, NULL);
    const char* endAddrStr = env->GetStringUTFChars(end_addr, NULL);


    auto reachableAddresses = pingRange(startAddrStr, endAddrStr);

    env->ReleaseStringUTFChars(start_addr, startAddrStr);
    env->ReleaseStringUTFChars(end_addr, endAddrStr);

    jobject arrayList = env->NewObject(arrayListClass, arrayListConstructor);
    for (const auto& addr : reachableAddresses) {
        jstring jAddr = env->NewStringUTF(addr.c_str());
        env->CallBooleanMethod(arrayList, arrayListAdd, jAddr);
    }

    return arrayList;
}
