# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
            src/main/cpp/aes.h
            src/main/cpp/aes_cbc.c
            src/main/cpp/aes_core.c
            src/main/cpp/aes_ecb.c
            src/main/cpp/aes_locl.h
            src/main/cpp/cbc128.c
            src/main/cpp/main_test.c
            src/main/cpp/modes.h
            src/main/cpp/native-lib.cpp)

find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

target_link_libraries( # Specifies the target library.
        native-lib
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )

IF (CMAKE_BUILD_TYPE STREQUAL Debug)
    ADD_DEFINITIONS(-DDEBUG)
ENDIF()