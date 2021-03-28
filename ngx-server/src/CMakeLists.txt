cmake_minimum_required(VERSION 3.14.2)

project(src)

set(NGX_SRC_FILE 
    core/ngx_config.cpp
    core/ngx_string.cpp
    core/ngx_log.cpp
    proc/ngx_daemon.cpp
    proc/ngx_proc.cpp
    proc/ngx_signal.cpp
    core/main.cpp
    )


include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/zlog
    )

add_executable(nginx ${NGX_SRC_FILE})
target_link_libraries(nginx)
