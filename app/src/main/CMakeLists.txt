cmake_minimum_required(VERSION 3.4.1)

include_directories(src/main/cpp/include)
link_directories(src/main/jniLibs/${ANDROID_ABI})

add_definitions(-DLINUX)
add_definitions(-DP2P_PPCS -DP2P_AUTH_ENABLE)

add_library(
    PPCSTunnelAgent
    SHARED
    src/main/cpp/AES/aes_core.cpp
    src/main/cpp/AES/aes_ecb.cpp
    src/main/cpp/AES/aes_misc.cpp
    src/main/cpp/memwatch.cpp
    src/main/cpp/P2PTunnel_Common.cpp
    src/main/cpp/P2PTunnel_Proto.cpp
    src/main/cpp/P2PTunnelAgent.cpp
    src/main/cpp/com_p2p_pppp_api_tunnel_P2PTunnelAgent.c
)

find_library(log-lib log)

target_link_libraries(
    PPCSTunnelAgent
    PPCS_API
    ${log-lib}
)
