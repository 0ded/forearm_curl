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
        // Extract parameters after '?'
        std::string paramString = uri.substr(pos + 1);

        // Tokenize the parameter string by '&'
        std::istringstream iss(paramString);
        std::string token;
        while (std::getline(iss, token, '&')) {
            // Tokenize each parameter by '=' to separate key and value
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

bool sendPing(const char* targetAddr) {
    int sockfd;
    struct sockaddr_in dest;
    struct icmp* icmp_hdr;
    char packet[PACKET_SIZE];

    // Create raw socket
    if ((sockfd = socket(AF_INET, SOCK_RAW, IPPROTO_ICMP)) < 0) {
        std::cerr << "Failed to create raw socket" << std::endl;
        return false;
    }

    // Set destination address
    std::memset(&dest, 0, sizeof(dest));
    dest.sin_family = AF_INET;
    if (inet_pton(AF_INET, targetAddr, &dest.sin_addr) <= 0) {
        std::cerr << "Invalid address: " << targetAddr << std::endl;
        close(sockfd);
        return false;
    }

    // Construct ICMP echo request packet
    icmp_hdr = (struct icmp*) packet;
    icmp_hdr->icmp_type = ICMP_ECHO_REQUEST;
    icmp_hdr->icmp_code = 0;
    icmp_hdr->icmp_id = getpid();
    icmp_hdr->icmp_seq = 0;
    std::memset(packet + sizeof(struct icmp), 'A', PACKET_SIZE - sizeof(struct icmp));

    // Send ICMP packet
    if (sendto(sockfd, packet, PACKET_SIZE, 0, (struct sockaddr*)&dest, sizeof(dest)) < 0) {
        std::cerr << "Failed to send ICMP packet" << std::endl;
        close(sockfd);
        return false;
    }

    close(sockfd);
    return true;
}


std::vector<std::string> pingRange(const std::string& startAddr, const std::string& endAddr) {
    std::vector<std::string> reachableAddresses;

    // Convert start and end addresses to integer representation
    unsigned int start = (startAddr[0] << 24) | (startAddr[1] << 16) | (startAddr[2] << 8) | startAddr[3];
    unsigned int end = (endAddr[0] << 24) | (endAddr[1] << 16) | (endAddr[2] << 8) | endAddr[3];

    // Ping each address in the range
    for (unsigned int i = start; i <= end; ++i) {
        // Convert integer back to IP address format
        std::ostringstream oss;
        oss << ((i >> 24) & 0xFF) << '.' << ((i >> 16) & 0xFF) << '.' << ((i >> 8) & 0xFF) << '.' << (i & 0xFF);
        std::string address = oss.str();

        std::string command = "ping -c 1 -w 1 " + address;
        bool response = sendPing(address.c_str());
        if (response) {
            reachableAddresses.push_back(address);
        }
    }

    return reachableAddresses;
}


extern "C" {
    JNIEXPORT jstring
    JNICALL
    Java_com_example_forearm_1curl_MainActivity_stringFromJNI(
            JNIEnv *env,
            jobject /* this */)
            {
        std::string hello = "Hello from C++";
        return env->NewStringUTF(hello.c_str());
    }

    JNIEXPORT jobject
    JNICALL
    Java_com_example_forearm_1curl_httpHub_pingRange(JNIEnv *env, jobject obj, jstring startAddr, jstring endAddr)
    {
        jclass arrayListClass = env->FindClass("java/util/ArrayList");
        jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
        jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

        const char* startAddrStr = env->GetStringUTFChars(startAddr, NULL);
        const char* endAddrStr = env->GetStringUTFChars(endAddr, NULL);


        auto reachableAddresses = pingRange(startAddrStr, endAddrStr);

        env->ReleaseStringUTFChars(startAddr, startAddrStr);
        env->ReleaseStringUTFChars(endAddr, endAddrStr);

        jobject arrayList = env->NewObject(arrayListClass, arrayListConstructor);
        for (const auto& addr : reachableAddresses) {
            jstring jAddr = env->NewStringUTF(addr.c_str());
            env->CallBooleanMethod(arrayList, arrayListAdd, jAddr);
        }

        return arrayList;
    }
}

