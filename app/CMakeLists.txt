cmake_minimum_required(VERSION 3.4.1)

add_library(
             native-lib
             SHARED
             src/main/cpp/native-lib.cpp
             src/main/cpp/FaceTrack.cpp)

include_directories(src/main/cpp/include)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}  -L${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}" )

add_subdirectory(src/main/cpp/FaceAlignment)

include_directories(src/main/cpp/FaceAlignment/include)

target_link_libraries(
                       native-lib
                       opencv_java3
                       seeta_fa_lib
                       log )