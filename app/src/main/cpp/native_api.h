#pragma once

#include <cstdint>

using HookFunType = int (*)(void *func, void *replace, void **backup);
using UnhookFunType = int (*)(void *func);
using NativeOnModuleLoaded = void (*)(const char *name, void *handle);

struct NativeAPIEntries {
    std::uint32_t version;
    HookFunType hookFunc;
    UnhookFunType unhookFunc;
};
