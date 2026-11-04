#pragma once
#include <cstdarg>
#include <cstdio>
enum { ANDROID_LOG_DEBUG, ANDROID_LOG_INFO, ANDROID_LOG_WARN, ANDROID_LOG_ERROR };
inline int __android_log_vprint(int, const char *, const char *format, va_list args) {
    const int result = std::vfprintf(stderr, format, args);
    std::fputc('\n', stderr);
    return result;
}
