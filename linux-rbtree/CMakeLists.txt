cmake_minimum_required(VERSION 3.6)
project(linux-rbtree)

set (CMAKE_C_COMPILER "/usr/bin/clang")
set (CMAKE_CXX_COMPILER "/usr/bin/clang++")

set(SRC_FILES  
	${CMAKE_SOURCE_DIR}/main.c
	${CMAKE_SOURCE_DIR}/rbtree.c
)

include_directories(${CMAKE_SOURCE_DIR})

add_executable(main ${SRC_FILES})
