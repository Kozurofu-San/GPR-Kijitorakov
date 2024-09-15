cmake_minimum_required(VERSION 3.15)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(DEPENDENCIES_FOLDER dependencies)

include(FetchContent)

# FetchContent_Populate(
#   googletest
#   GIT_REPOSITORY https://github.com/google/googletest.git
#   GIT_TAG        release-1.10.0
#     SOURCE_DIR ${CMAKE_SOURCE_DIR}/example1
# )

# FetchContent_Populate(
#   lvgl
#   GIT_REPOSITORY https://github.com/lvgl/lvgl.git
#   GIT_TAG        v9.2.0
#   GIT_PROGRESS TRUE
#   SOURCE_DIR ${CMAKE_SOURCE_DIR}/${DEPENDENCIES_FOLDER}/lvgl
# )

FetchContent_Populate(
  freertos
  GIT_REPOSITORY https://github.com/FreeRTOS/FreeRTOS-Kernel.git
  GIT_TAG        V11.1.0
  GIT_PROGRESS TRUE
  SOURCE_DIR ${CMAKE_SOURCE_DIR}/${DEPENDENCIES_FOLDER}/freertos
)


set(FREERTOS_KERNEL_PATH ${CMAKE_SOURCE_DIR}/${DEPENDENCIES_FOLDER}/freertos)

# Add the freertos_config for FreeRTOS-Kernel
add_library(freertos_config INTERFACE)

target_include_directories(freertos_config
    INTERFACE
    "../"
)

if (DEFINED FREERTOS_SMP_EXAMPLE AND FREERTOS_SMP_EXAMPLE STREQUAL "1")
    message(STATUS "Build FreeRTOS SMP example")
    # Adding the following configurations to build SMP template port
    add_compile_options( -DconfigNUMBER_OF_CORES=2 -DconfigUSE_PASSIVE_IDLE_HOOK=0 )
endif()

# Select the heap port.  values between 1-4 will pick a heap.
set(FREERTOS_HEAP "1" CACHE STRING "" FORCE)

# Select the native compile PORT
set(FREERTOS_PORT "TEMPLATE" CACHE STRING "" FORCE)

# Adding the FreeRTOS-Kernel subdirectory
add_subdirectory(${FREERTOS_KERNEL_PATH} FreeRTOS-Kernel)

target_link_libraries(${CMAKE_PROJECT_NAME} freertos_kernel freertos_config)

set_property(TARGET freertos_kernel PROPERTY C_STANDARD 23)

add_library(gpr STATIC)

target_include_directories(gpr PUBLIC
    "./"
)

target_sources(gpr PUBLIC
    Gpr.cpp
)