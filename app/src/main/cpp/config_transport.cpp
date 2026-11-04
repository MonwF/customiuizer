// SPDX-License-Identifier: GPL-3.0-or-later
// Existing-service transport pattern informed by WOMMO and NativeHookKit.
// Independent fixed-snapshot implementation; source links in docs/native-folders.md.
#include "folder_config.h"
#include <android/binder_ibinder.h>
#include <android/binder_parcel.h>
#include <android/log.h>
#include <atomic>
#include <cstdio>
#include <cstring>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h>

namespace {
constexpr transaction_code_t kTransaction = 0x0050464c;
constexpr int32_t kAck = 0x50464c31;
std::atomic<bool> started{false};
void *binder_library = nullptr; // Kept mapped: Binder classes outlive this worker.
void *OnCreate(void *p) { return p; }
void OnDestroy(void *) {}
binder_status_t OnTransact(AIBinder *, transaction_code_t, const AParcel *, AParcel *) {
    return STATUS_UNKNOWN_TRANSACTION;
}
struct BinderApi {
    AIBinder *(*check_service)(const char *) = nullptr;
    decltype(&AIBinder_getClass) get_class = nullptr;
    decltype(&AIBinder_Class_define) define_class = nullptr;
    decltype(&AIBinder_Class_getDescriptor) descriptor = nullptr;
    decltype(&AIBinder_associateClass) associate = nullptr;
    decltype(&AIBinder_prepareTransaction) prepare = nullptr;
    decltype(&AIBinder_transact) transact = nullptr;
    decltype(&AIBinder_decStrong) release = nullptr;
    decltype(&AParcel_delete) destroy = nullptr;
    decltype(&AParcel_getDataSize) size = nullptr;
    decltype(&AParcel_readInt32) read = nullptr;
    bool load() {
        binder_library = dlopen("libbinder_ndk.so", RTLD_NOW | RTLD_LOCAL);
        if (!binder_library) return false;
#define RESOLVE(field, symbol) field = reinterpret_cast<decltype(field)>(dlsym(binder_library, symbol)); if (!field) return false
        RESOLVE(check_service, "AServiceManager_checkService");
        RESOLVE(get_class, "AIBinder_getClass");
        RESOLVE(define_class, "AIBinder_Class_define");
        RESOLVE(descriptor, "AIBinder_Class_getDescriptor");
        RESOLVE(associate, "AIBinder_associateClass");
        RESOLVE(prepare, "AIBinder_prepareTransaction");
        RESOLVE(transact, "AIBinder_transact");
        RESOLVE(release, "AIBinder_decStrong");
        RESOLVE(destroy, "AParcel_delete");
        RESOLVE(size, "AParcel_getDataSize");
        RESOLVE(read, "AParcel_readInt32");
#undef RESOLVE
        return true;
    }
    bool fetch(FolderConfig *config) {
        AIBinder *binder = check_service("clipboard");
        if (!binder) return false;
        const AIBinder_Class *cls = get_class(binder);
        static AIBinder_Class *client_class = nullptr;
        bool ok = true;
        if (cls) {
            const char *name = descriptor(cls);
            ok = name && std::strcmp(name, "android.content.IClipboard") == 0;
        } else {
            if (!client_class) client_class = define_class("android.content.IClipboard", OnCreate, OnDestroy, OnTransact);
            ok = client_class && associate(binder, client_class);
        }
        AParcel *request = nullptr, *reply = nullptr;
        if (ok) ok = prepare(binder, &request) == STATUS_OK;
        if (ok) ok = transact(binder, kTransaction, &request, &reply, 0) == STATUS_OK;
        int32_t values[7]{};
        if (ok) ok = reply && size(reply) == static_cast<int32_t>(sizeof(values));
        for (auto &value : values) if (ok) ok = read(reply, &value) == STATUS_OK;
        if (request) destroy(request);
        if (reply) destroy(reply);
        release(binder);
        if (!ok || values[0] != kAck || values[1] != 1 || values[2] < 0 || values[2] > 3) return false;
        FolderConfig candidate{(values[2] & 1) != 0, static_cast<uint32_t>(values[3]),
            static_cast<uint32_t>(values[4]), static_cast<uint32_t>(values[5]),
            static_cast<uint32_t>(values[6]), (values[2] & 2) != 0};
        if (!ValidFolderConfig(candidate)) return false;
        *config = candidate;
        return true;
    }
};
void *ReadConfig(void *) {
    BinderApi api;
    if (api.load()) {
        for (int attempt = 0; attempt < 5; ++attempt) {
            FolderConfig config;
            if (api.fetch(&config)) {
                const bool accepted = pengeek_folder_configure(&config);
                __android_log_print(ANDROID_LOG_INFO, "PengeekFolders", "config snapshot accepted=%d layout=%d marker=%d", accepted, config.enabled, config.hide_new_marker);
                return nullptr;
            }
            sleep(2);
        }
    }
    __android_log_print(ANDROID_LOG_WARN, "PengeekFolders", "config unavailable; native features left inactive");
    return nullptr;
}
}
void StartFolderConfigTransport() {
    char name[256]{};
    FILE *file = std::fopen("/proc/self/cmdline", "rb");
    if (!file) return;
    const auto count = std::fread(name, 1, sizeof(name) - 1, file);
    std::fclose(file);
    if (!count || std::strcmp(name, "com.miui.home") != 0 || started.exchange(true)) return;
    pthread_t worker;
    if (pthread_create(&worker, nullptr, ReadConfig, nullptr) == 0) pthread_detach(worker);
    else started.store(false);
}
