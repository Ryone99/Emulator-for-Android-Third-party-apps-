cmake_minimum_required(VERSION 3.22.1)

project("sandbox_core")

add_library(
    sandbox_core
    SHARED
    sandbox_core.cpp
    hook_engine.cpp
    network_interceptor.cpp
    spoof_engine.cpp
)

find_library(
    log-lib
    log
)

find_library(
    android-lib
    android
)

find_library(
    glesv3-lib
    GLESv3
)

target_include_directories(sandbox_core PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(
    sandbox_core
    ${log-lib}
    ${android-lib}
    ${glesv3-lib}
)
