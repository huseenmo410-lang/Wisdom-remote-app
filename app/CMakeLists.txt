cmake_minimum_required(VERSION 3.22.1)

project("tvremote")

add_library(
        tvremote
        SHARED
        src/main/cpp/tv_socket.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        tvremote
        ${log-lib}
)
