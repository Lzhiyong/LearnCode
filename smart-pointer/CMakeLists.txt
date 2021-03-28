cmake_minimum_required(VERSION 3.14.2)
project(smart-pointer)

set (CMAKE_C_COMPILER "/usr/bin/clang")
set (CMAKE_CXX_COMPILER "/usr/bin/clang++")

set(SRC_FILES  
	${CMAKE_SOURCE_DIR}/main.cpp
	${CMAKE_SOURCE_DIR}/RefBase.cpp
)

include_directories(${CMAKE_SOURCE_DIR}/include)

add_executable(main ${SRC_FILES})