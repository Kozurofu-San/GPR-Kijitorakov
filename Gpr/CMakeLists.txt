cmake_minimum_required(VERSION 3.15)


if (CMAKE_VERSION VERSION_LESS "3.1")
    if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
      set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
    endif ()
  else ()
    set (CMAKE_CXX_STANDARD 11)
  endif ()

add_library(gpr STATIC)

target_include_directories(gpr PUBLIC
    "./"
)

target_sources(gpr PUBLIC
    Gpr.cpp
)