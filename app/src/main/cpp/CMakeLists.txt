cmake_minimum_required(VERSION 3.22.1)
project(pengeek_folder_native LANGUAGES CXX ASM)

add_library(pengeek_folder_native SHARED
    folder_module.cpp
    config_transport.cpp
    dart_hooks.S
)

target_compile_features(pengeek_folder_native PRIVATE cxx_std_20)
target_compile_options(pengeek_folder_native PRIVATE
    -Wall
    -Wextra
    -Werror
    -fvisibility=hidden
    -fvisibility-inlines-hidden
    -fno-exceptions
    -fno-rtti
)
target_link_options(pengeek_folder_native PRIVATE
    -Wl,--build-id=sha1
    -Wl,--no-undefined
    -Wl,-z,max-page-size=16384
    -Wl,-z,now
    -Wl,-z,relro
)
target_link_libraries(pengeek_folder_native PRIVATE log dl)
