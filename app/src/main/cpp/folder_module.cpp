#include "folder_config.h"
#include "native_api.h"
#include "target_profiles.h"

#include <android/log.h>
#include <elf.h>
#include <fcntl.h>
#include <pthread.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>

#include <array>
#include <atomic>
#include <cerrno>
#include <cinttypes>
#include <cstdarg>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>

namespace {

constexpr char kTag[] = "CustoMiUIzerFolder";
constexpr char kModuleBuild[] = "configurable-native-1";
constexpr char kTargetProcess[] = "com.miui.home";
constexpr char kRowNumSymbol[] = "FolderClingGetxController.rowNum";
constexpr char kSetupViewSymbol[] = "FolderAnimController._setupView";
constexpr char kGetChildCountSymbol[] =
    "FolderGridViewGetxController.getChildCount";
std::uint32_t kTargetChildInstruction = 0xd2800110U;
FolderConfig g_config{};
std::atomic<bool> g_configured{false};
pthread_mutex_t g_config_mutex = PTHREAD_MUTEX_INITIALIZER;
constexpr std::size_t kInlineEntryReadbackSize = 16;
constexpr std::size_t kExecutablePageSize = 4096;
constexpr std::size_t kMaxAppImages = 16;
constexpr std::uint32_t kLoadAbsoluteTarget = 0x58000050U;
constexpr std::uint32_t kBranchAbsoluteTarget = 0xd61f0200U;

std::atomic<bool> g_worker_started{false};
std::atomic<bool> g_contract_committed{false};
std::atomic<bool> g_write_started{false};
std::atomic<bool> g_rollback_incomplete{false};
std::atomic<bool> g_child_patch_owned{false};
void *g_child_instruction_address = nullptr;
pthread_mutex_t g_instruction_patch_mutex = PTHREAD_MUTEX_INITIALIZER;

extern "C" {

__attribute__((visibility("hidden"))) volatile std::uint32_t
    hc_contract_active = 0;
__attribute__((visibility("hidden"))) volatile std::uint32_t
    hc_buffer_rows = 8;
__attribute__((visibility("hidden"))) volatile std::uint32_t
    hc_pool_base_offset = 0;
__attribute__((visibility("hidden"))) volatile std::uint32_t
    hc_columns_slot_offset = 0;

__attribute__((visibility("hidden"))) void *hc_row_num_original = nullptr;
__attribute__((visibility("hidden"))) void *hc_setup_view_original = nullptr;
__attribute__((visibility("hidden"))) double hc_density_ratio = 1.0;
__attribute__((visibility("hidden"))) double hc_vertical_gap = 0.0;
__attribute__((visibility("hidden"))) std::uint32_t hc_columns_smi = 6;


__attribute__((visibility("hidden"))) volatile std::uint64_t
    folder_diag_contract_active = 0;
__attribute__((visibility("hidden"))) volatile std::uint64_t
    folder_diag_row_num_calls = 0;
__attribute__((visibility("hidden"))) volatile std::uint64_t
    folder_diag_row_num_last = 0;
__attribute__((visibility("hidden"))) volatile std::uint64_t
    folder_diag_setup_calls = 0;
__attribute__((visibility("hidden"))) volatile std::uint64_t
    folder_diag_pool_value_last = 0;
__attribute__((visibility("hidden"))) volatile std::uint64_t
    folder_diag_pool_invalid_last = 0;
__attribute__((visibility("hidden"))) volatile std::uint64_t
    folder_diag_child_patch_offset = 0;
__attribute__((visibility("hidden"))) volatile std::uint64_t
    folder_diag_child_instruction_last = 0;

void hc_row_num_hook();
void hc_setup_view_hook();
void hc_compact_spacing_exit();
void hc_density_exit();
void hc_density_cache();
__attribute__((visibility("hidden"))) void *hc_density_cache_resume = nullptr;

}  // extern "C"

void Log(int priority, const char *format, ...) {
    va_list args;
    va_start(args, format);
    __android_log_vprint(priority, kTag, format, args);
    va_end(args);
}

#define LOGD(...) Log(ANDROID_LOG_DEBUG, __VA_ARGS__)
#define LOGI(...) Log(ANDROID_LOG_INFO, __VA_ARGS__)
#define LOGW(...) Log(ANDROID_LOG_WARN, __VA_ARGS__)
#define LOGE(...) Log(ANDROID_LOG_ERROR, __VA_ARGS__)

struct ResolvedSymbol {
    void *address = nullptr;
    std::size_t size = 0;
};

struct AppImage {
    std::uintptr_t base = 0;
    std::size_t size = 0;
    std::uintptr_t executable_offset = 0;
    std::size_t executable_size = 0;
    std::uintptr_t file_offset = 0;
    const TargetProfile *profile = nullptr;
    bool optimistic = false;
    char build_id[129]{};
    char path[512]{};
};

struct AppImages {
    std::array<AppImage, kMaxAppImages> items{};
    std::size_t count = 0;
};

bool ReadExactAt(int fd, void *buffer, std::size_t size, off_t offset) {
    auto *out = static_cast<unsigned char *>(buffer);
    std::size_t total = 0;
    while (total < size) {
        const ssize_t count = pread(
            fd, out + total, size - total,
            offset + static_cast<off_t>(total));
        if (count < 0 && errno == EINTR) continue;
        if (count <= 0) return false;
        total += static_cast<std::size_t>(count);
    }
    return true;
}

bool ParseBuildIdNotes(const unsigned char *notes, std::size_t size,
                       char *hex, std::size_t hex_size) {
    std::size_t cursor = 0;
    while (cursor + sizeof(Elf64_Nhdr) <= size) {
        Elf64_Nhdr note{};
        std::memcpy(&note, notes + cursor, sizeof(note));
        cursor += sizeof(note);
        const std::size_t name_aligned = (note.n_namesz + 3U) & ~3U;
        const std::size_t desc_aligned = (note.n_descsz + 3U) & ~3U;
        if (cursor + name_aligned + desc_aligned > size) return false;
        const char *name = reinterpret_cast<const char *>(notes + cursor);
        const unsigned char *description = notes + cursor + name_aligned;
        if (note.n_type == NT_GNU_BUILD_ID && note.n_namesz >= 3 &&
            std::memcmp(name, "GNU", 3) == 0 &&
            hex_size >= static_cast<std::size_t>(note.n_descsz) * 2U + 1U) {
            for (std::uint32_t index = 0; index < note.n_descsz; ++index) {
                std::snprintf(
                    hex + index * 2U, hex_size - index * 2U,
                    "%02x", description[index]);
            }
            hex[note.n_descsz * 2U] = '\0';
            return true;
        }
        cursor += name_aligned + desc_aligned;
    }
    return false;
}

bool ReadBuildIdFileAt(const char *path, off_t elf_offset, char *hex,
                       std::size_t hex_size,
                       std::uintptr_t *first_load_vaddr,
                       std::size_t *image_size,
                       std::uintptr_t *executable_vaddr,
                       std::size_t *executable_size) {
    if (first_load_vaddr != nullptr) *first_load_vaddr = 0;
    if (image_size != nullptr) *image_size = 0;
    if (executable_vaddr != nullptr) *executable_vaddr = 0;
    if (executable_size != nullptr) *executable_size = 0;
    const int fd = open(path, O_RDONLY | O_CLOEXEC);
    if (fd < 0) return false;

    Elf64_Ehdr header{};
    bool valid = ReadExactAt(fd, &header, sizeof(header), elf_offset);
    if (!valid || std::memcmp(header.e_ident, ELFMAG, SELFMAG) != 0 ||
        header.e_ident[EI_CLASS] != ELFCLASS64 ||
        header.e_ident[EI_DATA] != ELFDATA2LSB ||
        header.e_machine != EM_AARCH64 ||
        header.e_phentsize != sizeof(Elf64_Phdr) || header.e_phnum == 0 ||
        header.e_phnum >= 256 || header.e_phoff > 64U * 1024U) {
        close(fd);
        return false;
    }

    bool found_load = false;
    bool found_build_id = false;
    std::uintptr_t first_load = 0;
    std::uintptr_t last_load_end = 0;
    std::uintptr_t first_executable = 0;
    std::uintptr_t last_executable_end = 0;
    for (Elf64_Half index = 0; index < header.e_phnum; ++index) {
        Elf64_Phdr program{};
        const off_t program_offset = elf_offset +
            static_cast<off_t>(header.e_phoff) +
            static_cast<off_t>(index) * sizeof(program);
        if (!ReadExactAt(fd, &program, sizeof(program), program_offset)) break;
        if (!found_load && program.p_type == PT_LOAD) {
            first_load = static_cast<std::uintptr_t>(program.p_vaddr);
            found_load = true;
        }
        if (program.p_type == PT_LOAD &&
            program.p_vaddr <= UINTPTR_MAX - program.p_memsz) {
            const auto end = static_cast<std::uintptr_t>(
                program.p_vaddr + program.p_memsz);
            if (end > last_load_end) last_load_end = end;
            if ((program.p_flags & PF_X) != 0) {
                if (first_executable == 0 ||
                    program.p_vaddr < first_executable) {
                    first_executable =
                        static_cast<std::uintptr_t>(program.p_vaddr);
                }
                if (end > last_executable_end) last_executable_end = end;
            }
        }
        if (program.p_type != PT_NOTE || program.p_filesz == 0 ||
            program.p_filesz > 1024U * 1024U) {
            continue;
        }
        auto *notes = static_cast<unsigned char *>(
            std::malloc(static_cast<std::size_t>(program.p_filesz)));
        if (notes == nullptr) continue;
        valid = ReadExactAt(
                    fd, notes, static_cast<std::size_t>(program.p_filesz),
                    elf_offset + static_cast<off_t>(program.p_offset)) &&
            ParseBuildIdNotes(
                notes, static_cast<std::size_t>(program.p_filesz),
                hex, hex_size);
        std::free(notes);
        if (valid) found_build_id = true;
    }
    close(fd);
    if (!found_load || !found_build_id || last_load_end <= first_load ||
        last_executable_end <= first_executable ||
        first_executable < first_load) {
        return false;
    }
    if (first_load_vaddr != nullptr) *first_load_vaddr = first_load;
    if (image_size != nullptr) {
        *image_size = static_cast<std::size_t>(last_load_end - first_load);
    }
    if (executable_vaddr != nullptr) *executable_vaddr = first_executable;
    if (executable_size != nullptr) {
        *executable_size = static_cast<std::size_t>(
            last_executable_end - first_executable);
    }
    return true;
}

bool ReadProcText(const char *path, char *buffer, std::size_t size) {
    if (buffer == nullptr || size < 2) return false;
    const int fd = open(path, O_RDONLY | O_CLOEXEC);
    if (fd < 0) return false;
    const ssize_t count = read(fd, buffer, size - 1U);
    close(fd);
    if (count <= 0) return false;
    buffer[count] = '\0';
    for (ssize_t index = 0; index < count; ++index) {
        if (buffer[index] == '\0' || buffer[index] == '\n') {
            buffer[index] = ' ';
        }
    }
    return true;
}

bool ReadFileRaw(const char *path, char *buffer, std::size_t size,
                 std::size_t *bytes_read, bool *truncated) {
    if (bytes_read != nullptr) *bytes_read = 0;
    if (truncated != nullptr) *truncated = false;
    if (buffer == nullptr || size < 2) return false;
    const int fd = open(path, O_RDONLY | O_CLOEXEC);
    if (fd < 0) return false;
    std::size_t total = 0;
    while (total + 1U < size) {
        const ssize_t count = read(fd, buffer + total, size - 1U - total);
        if (count < 0 && errno == EINTR) continue;
        if (count <= 0) break;
        total += static_cast<std::size_t>(count);
    }
    if (total + 1U == size) {
        char extra = 0;
        const ssize_t count = read(fd, &extra, 1);
        if (truncated != nullptr) *truncated = count > 0;
    }
    close(fd);
    if (total == 0) return false;
    buffer[total] = '\0';
    if (bytes_read != nullptr) *bytes_read = total;
    return true;
}

bool IsTargetProcess(char *cmdline, std::size_t size) {
    if (!ReadProcText("/proc/self/cmdline", cmdline, size)) return false;
    return std::strcmp(cmdline, kTargetProcess) == 0 ||
        std::strncmp(cmdline, "com.miui.home ", 14) == 0;
}

bool HasApkSuffix(const char *path) {
    if (path == nullptr) return false;
    const std::size_t length = std::strlen(path);
    return length > 4U && std::strcmp(path + length - 4U, ".apk") == 0;
}

bool IsMiuiHomeApkPath(const char *path) {
    if (!HasApkSuffix(path)) return false;
    const std::size_t length = std::strlen(path);
    constexpr char kSystemSuffix[] = "/MiuiHome.apk";
    constexpr char kDataSuffix[] = "/base.apk";
    const bool system_apk = length >= sizeof(kSystemSuffix) - 1U &&
        std::strcmp(path + length - (sizeof(kSystemSuffix) - 1U),
                    kSystemSuffix) == 0;
    const bool data_apk = length >= sizeof(kDataSuffix) - 1U &&
        std::strcmp(path + length - (sizeof(kDataSuffix) - 1U),
                    kDataSuffix) == 0 &&
        std::strstr(path, "/com.miui.home-") != nullptr;
    return system_apk || data_apk;
}

AppImages FindAppImages(bool detailed) {
    AppImages images{};
    constexpr std::size_t kMapsCapacity = 1U << 20U;
    auto *maps = static_cast<char *>(std::malloc(kMapsCapacity));
    if (maps == nullptr) {
        LOGE("image_scan allocation_failed capacity=%zu", kMapsCapacity);
        return images;
    }

    std::size_t maps_bytes = 0;
    bool maps_truncated = false;
    std::size_t lines_seen = 0;
    std::size_t apk_lines = 0;
    std::size_t elf_candidates = 0;
    std::size_t unsupported_candidates = 0;
    if (ReadFileRaw(
            "/proc/self/maps", maps, kMapsCapacity,
            &maps_bytes, &maps_truncated) && !maps_truncated) {
        for (char *line = maps; *line != '\0';) {
            ++lines_seen;
            char *newline = std::strchr(line, '\n');
            if (newline != nullptr) *newline = '\0';

            std::uintptr_t start = 0;
            std::uintptr_t end = 0;
            std::uintptr_t file_offset = 0;
            char permissions[8]{};
            char path[512]{};
            const int matched = std::sscanf(
                line, "%" SCNxPTR "-%" SCNxPTR " %7s %" SCNxPTR
                      " %*s %*s %511s",
                &start, &end, permissions, &file_offset, path);
            const bool target_apk = matched >= 5 && permissions[0] == 'r' &&
                path[0] == '/' && IsMiuiHomeApkPath(path);
            if (target_apk) {
                ++apk_lines;
                char build_id[129]{};
                std::uintptr_t first_load_vaddr = 0;
                std::size_t image_size = 0;
                std::uintptr_t executable_vaddr = 0;
                std::size_t executable_size = 0;
                if (ReadBuildIdFileAt(
                        path, static_cast<off_t>(file_offset), build_id,
                        sizeof(build_id), &first_load_vaddr, &image_size,
                        &executable_vaddr, &executable_size)) {
                    ++elf_candidates;
                    const TargetProfile *profile = FindTargetProfile(build_id);
                    bool optimistic = false;
                    if (profile == nullptr && !kTargetProfiles.empty()) {
                        // Minor MiuiHome updates frequently change only the
                        // image identity or shift code without changing the
                        // validated Dart layout. Try the newest semantic
                        // template; InstallFolderContract still refuses any
                        // incompatible instruction relationships.
                        profile = &kTargetProfiles.front();
                        optimistic = true;
                    }
                    if (profile != nullptr && images.count < images.items.size()) {
                        AppImage &image = images.items[images.count++];
                        image.base = start - first_load_vaddr;
                        image.size = image_size;
                        image.executable_offset =
                            executable_vaddr - first_load_vaddr;
                        image.executable_size = executable_size;
                        image.file_offset = file_offset;
                        image.profile = profile;
                        image.optimistic = optimistic;
                        std::snprintf(
                            image.build_id, sizeof(image.build_id), "%s",
                            build_id);
                        std::snprintf(
                            image.path, sizeof(image.path), "%s", path);
                        if (optimistic) {
                            LOGW(
                                "image_scan optimistic_profile build_id=%s "
                                "template=%s path=%s",
                                build_id, profile->version_name, path);
                        }
                        LOGI(
                            "image_candidate index=%zu profile=%s build_id=%s "
                            "base=%p size=0x%zx executable=0x%" PRIxPTR
                            "+0x%zx apk_offset=0x%" PRIxPTR,
                            images.count - 1U, profile->version_name,
                            image.build_id,
                            reinterpret_cast<void *>(image.base), image.size,
                            image.executable_offset, image.executable_size,
                            image.file_offset);
                    } else {
                        ++unsupported_candidates;
                    }
                }
            }

            if (newline == nullptr) break;
            line = newline + 1;
        }
    }
    std::free(maps);

    if (detailed || images.count != 0) {
        LOGI(
            "image_scan bytes=%zu truncated=%d lines=%zu apk_maps=%zu "
            "elf_candidates=%zu unsupported=%zu candidates=%zu",
            maps_bytes, maps_truncated, lines_seen, apk_lines,
            elf_candidates, unsupported_candidates, images.count);
    }
    return images;
}

std::uint32_t ReadInstruction(const void *address) {
    std::uint32_t value = 0;
    std::memcpy(&value, address, sizeof(value));
    return value;
}

struct MaskedInstruction {
    std::uint32_t expected;
    std::uint32_t mask;
};

constexpr std::array<MaskedInstruction, 14> kSetupViewPattern{{
    {0xa9bf79fdU, 0xffffffffU},
    {0xaa0f03fdU, 0xffffffffU},
    {0xd100c1efU, 0xffffffffU},
    {0xf81f83a1U, 0xffffffffU},
    {0xf81f03a2U, 0xffffffffU},
    {0xd2800041U, 0xffffffffU},
    {0x94000000U, 0xfc000000U},
    {0xaa0003e2U, 0xffffffffU},
    {0xf85f83a0U, 0xffffffffU},
    {0xf81e83a2U, 0xffffffffU},
    {0xb800f040U, 0xffffffffU},
    {0xb84bb001U, 0xffffffffU},
    {0x8b1c8021U, 0xffffffffU},
    {0x94000000U, 0xfc000000U},
}};

constexpr std::array<MaskedInstruction, 7> kRowNumPattern{{
    // Receiver field relocation is compatible; keep opcode, base and target.
    {0xb8400022U, 0xffe00fffU},
    {0x8b1c8042U, 0xffffffffU},
    {0xf9402370U, 0xffffffffU},
    {0x6b10005fU, 0xffffffffU},
    {0x54000060U, 0xffffffffU},
    {0xf8463040U, 0xffffffffU},
    {0xd65f03c0U, 0xffffffffU},
}};

constexpr std::array<MaskedInstruction, 12> kGetChildCountPattern{{
    {0xa9bf79fdU, 0xffffffffU},
    {0xaa0f03fdU, 0xffffffffU},
    {0xd10061efU, 0xffffffffU},
    {0xaa0103e0U, 0xffffffffU},
    {0xf81f83a1U, 0xffffffffU},
    {0x94000000U, 0xfc000000U},
    {0xf81f03a0U, 0xffffffffU},
    {0xf9403f40U, 0xffffffffU},
    {0x00000000U, 0x00000000U},
    {0xf9402370U, 0xffffffffU},
    {0x6b10001fU, 0xffffffffU},
    {0x54000081U, 0xffffffffU},
}};

template <std::size_t Size>
bool FindUniqueMaskedPattern(
        const void *start, std::size_t byte_count,
        const std::array<MaskedInstruction, Size> &pattern,
        std::uintptr_t upper_bound, void **result) {
    if (start == nullptr || result == nullptr ||
        (reinterpret_cast<std::uintptr_t>(start) & 3U) != 0U ||
        byte_count < Size * sizeof(std::uint32_t)) {
        return false;
    }
    *result = nullptr;
    const auto begin = reinterpret_cast<std::uintptr_t>(start);
    const std::size_t last =
        byte_count - Size * sizeof(std::uint32_t);
    for (std::size_t offset = 0; offset <= last;
         offset += sizeof(std::uint32_t)) {
        const std::uintptr_t address = begin + offset;
        if (upper_bound != 0 && address >= upper_bound) break;
        bool matches = true;
        for (std::size_t index = 0; index < Size; ++index) {
            const std::uint32_t word = ReadInstruction(
                reinterpret_cast<const void *>(
                    address + index * sizeof(std::uint32_t)));
            if ((word & pattern[index].mask) != pattern[index].expected) {
                matches = false;
                break;
            }
        }
        if (!matches) continue;
        if (*result != nullptr) {
            LOGE(
                "semantic_scan ambiguous first=%p second=%p words=%zu",
                *result, reinterpret_cast<void *>(address), Size);
            *result = nullptr;
            return false;
        }
        *result = reinterpret_cast<void *>(address);
    }
    return *result != nullptr;
}

bool DecodeBlTarget(std::uintptr_t address, std::uint32_t instruction,
                    std::uintptr_t *target);

bool FindRowCallee(const AppImage &image, const void *setup, void **result) {
    const auto start = reinterpret_cast<std::uintptr_t>(setup);
    const auto text_start = image.base + image.executable_offset;
    const auto text_end = text_start + image.executable_size;
    *result = nullptr;
    for (std::size_t offset = 0; offset < 0x324 && start + offset + 4 <= text_end; offset += 4) {
        std::uintptr_t callee = 0;
        if (!DecodeBlTarget(start + offset,
                ReadInstruction(reinterpret_cast<const void *>(start + offset)), &callee) ||
            callee < text_start || callee > text_end - kRowNumPattern.size() * 4) continue;
        bool matches = true;
        for (std::size_t i = 0; i < kRowNumPattern.size(); ++i) {
            const auto &word = kRowNumPattern[i];
            matches = matches && (ReadInstruction(reinterpret_cast<const void *>(callee + i * 4)) &
                                   word.mask) == word.expected;
        }
        if (!matches) continue;
        if (*result != nullptr && reinterpret_cast<std::uintptr_t>(*result) != callee) return false;
        *result = reinterpret_cast<void *>(callee);
    }
    return *result != nullptr;
}

bool ResolveSemanticSymbols(const AppImage &image,
                            ResolvedSymbol *setup_view,
                            ResolvedSymbol *row_num,
                            ResolvedSymbol *get_child_count) {
    if (setup_view == nullptr || row_num == nullptr ||
        get_child_count == nullptr || image.base == 0 || image.size == 0 ||
        image.executable_size == 0 ||
        image.executable_offset >= image.size ||
        image.executable_size > image.size - image.executable_offset ||
        image.base > UINTPTR_MAX - image.executable_offset) {
        return false;
    }
    const auto executable = image.base + image.executable_offset;
    void *setup_address = nullptr;
    void *row_address = nullptr;
    void *child_address = nullptr;
    if (!FindUniqueMaskedPattern(
            reinterpret_cast<const void *>(executable),
            image.executable_size, kSetupViewPattern, 0, &setup_address) ||
        !FindRowCallee(image, setup_address, &row_address) ||
        !FindUniqueMaskedPattern(
            reinterpret_cast<const void *>(executable),
            image.executable_size, kGetChildCountPattern,
            reinterpret_cast<std::uintptr_t>(setup_address),
            &child_address)) {
        LOGD(
            "semantic_scan refused build_id=%s setup=%p row=%p child=%p",
            image.build_id, setup_address, row_address, child_address);
        return false;
    }
    *setup_view = {setup_address, 0x324U};
    *row_num = {row_address, 0x30U};
    *get_child_count = {child_address, 0xb4U};
    LOGI(
        "semantic_scan resolved build_id=%s setup=0x%" PRIxPTR
        " row=0x%" PRIxPTR " child=0x%" PRIxPTR,
        image.build_id,
        reinterpret_cast<std::uintptr_t>(setup_address) - image.base,
        reinterpret_cast<std::uintptr_t>(row_address) - image.base,
        reinterpret_cast<std::uintptr_t>(child_address) - image.base);
    return true;
}

bool DecodeLdrUnsignedX(std::uint32_t instruction, unsigned *target,
                        unsigned *base, std::uint32_t *offset) {
    if ((instruction & 0xffc00000U) != 0xf9400000U) return false;
    *target = instruction & 0x1fU;
    *base = (instruction >> 5U) & 0x1fU;
    *offset = ((instruction >> 10U) & 0xfffU) << 3U;
    return true;
}

bool DecodeMovzX(std::uint32_t instruction, unsigned *destination,
                 std::uint32_t *value) {
    if ((instruction & 0xff800000U) != 0xd2800000U ||
        ((instruction >> 21U) & 3U) != 0) {
        return false;
    }
    *destination = instruction & 0x1fU;
    *value = (instruction >> 5U) & 0xffffU;
    return true;
}

bool DecodeMulX(std::uint32_t instruction, unsigned *destination,
                unsigned *left, unsigned *right) {
    if ((instruction & 0xffe0fc00U) != 0x9b007c00U) return false;
    *destination = instruction & 0x1fU;
    *left = (instruction >> 5U) & 0x1fU;
    *right = (instruction >> 16U) & 0x1fU;
    return true;
}

bool DecodeBlTarget(std::uintptr_t pc, std::uint32_t instruction,
                    std::uintptr_t *target) {
    if ((instruction & 0xfc000000U) != 0x94000000U) return false;
    std::int64_t immediate =
        static_cast<std::int64_t>(instruction & 0x03ffffffU);
    if ((immediate & 0x02000000LL) != 0) immediate -= 0x04000000LL;
    *target = static_cast<std::uintptr_t>(
        static_cast<std::int64_t>(pc) + (immediate << 2));
    return true;
}

bool FindUniqueCall(const ResolvedSymbol &owner,
                    const ResolvedSymbol &callee,
                    std::size_t *instruction_index) {
    if (owner.address == nullptr || owner.size < 4 ||
        callee.address == nullptr || instruction_index == nullptr) {
        return false;
    }
    const auto start = reinterpret_cast<std::uintptr_t>(owner.address);
    const auto wanted = reinterpret_cast<std::uintptr_t>(callee.address);
    const std::size_t count = owner.size / sizeof(std::uint32_t);
    bool found = false;
    std::size_t result = 0;
    for (std::size_t index = 0; index < count; ++index) {
        std::uintptr_t target = 0;
        if (DecodeBlTarget(
                start + index * 4U,
                ReadInstruction(
                    reinterpret_cast<const void *>(start + index * 4U)),
                &target) && target == wanted) {
            if (found) return false;
            found = true;
            result = index;
        }
    }
    if (found) *instruction_index = result;
    return found;
}

bool DeriveSetupLayout(const ResolvedSymbol &setup,
                       const ResolvedSymbol &row_num,
                       std::uint32_t *pool_base_offset,
                       std::uint32_t *columns_slot_offset) {
    if (pool_base_offset == nullptr || columns_slot_offset == nullptr) {
        return false;
    }
    std::size_t call_index = 0;
    if (!FindUniqueCall(setup, row_num, &call_index) || call_index < 2) {
        return false;
    }
    const auto start = reinterpret_cast<std::uintptr_t>(setup.address);
    bool found = false;
    std::uint32_t pool = 0;
    std::uint32_t slot = 0;
    const std::size_t first = call_index > 24 ? call_index - 24 : 0;
    for (std::size_t index = first + 1; index < call_index; ++index) {
        unsigned target1 = 0;
        unsigned base1 = 0;
        unsigned target2 = 0;
        unsigned base2 = 0;
        std::uint32_t offset1 = 0;
        std::uint32_t offset2 = 0;
        if (!DecodeLdrUnsignedX(
                ReadInstruction(reinterpret_cast<const void *>(
                    start + (index - 1U) * 4U)),
                &target1, &base1, &offset1) ||
            !DecodeLdrUnsignedX(
                ReadInstruction(reinterpret_cast<const void *>(
                    start + index * 4U)),
                &target2, &base2, &offset2) ||
            base1 != 26 || base2 != target1) {
            continue;
        }
        if (found) return false;
        found = true;
        pool = offset1;
        slot = offset2;
    }
    if (!found) {
        return false;
    }
    *pool_base_offset = pool;
    *columns_slot_offset = slot;
    return true;
}

// Recover operands from the image when another hook has replaced their live
// load instruction. This discovers data; it never compares eligibility or
// writes original APK bytes back over somebody else's hook.
bool DeriveOriginalSetupLayout(const AppImage &image, const ResolvedSymbol &setup,
                               const ResolvedSymbol &row_num,
                               std::uint32_t *pool, std::uint32_t *slot) {
    auto *bytes = static_cast<unsigned char *>(std::malloc(setup.size));
    if (bytes == nullptr) return false;
    const int fd = open(image.path, O_RDONLY | O_CLOEXEC);
    const auto offset = reinterpret_cast<std::uintptr_t>(setup.address) - image.base;
    const bool read = fd >= 0 && ReadExactAt(fd, bytes, setup.size,
        static_cast<off_t>(image.file_offset + offset));
    if (fd >= 0) close(fd);
    const auto rebased_row = reinterpret_cast<std::uintptr_t>(bytes) +
        reinterpret_cast<std::uintptr_t>(row_num.address) -
        reinterpret_cast<std::uintptr_t>(setup.address);
    const bool derived = read && DeriveSetupLayout({bytes, setup.size},
        {reinterpret_cast<void *>(rebased_row), row_num.size}, pool, slot);
    std::free(bytes);
    return derived;
}

struct ChildPatchSite {
    void *address = nullptr;
    std::uint32_t instruction = 0;
    std::uint32_t offset = 0;
};

bool DeriveChildPatchSite(const ResolvedSymbol &get_child_count,
                          ChildPatchSite *site) {
    if (site == nullptr || get_child_count.address == nullptr ||
        get_child_count.size < 8) {
        return false;
    }
    const auto start =
        reinterpret_cast<std::uintptr_t>(get_child_count.address);
    const std::size_t count = get_child_count.size / 4U;
    bool found = false;
    ChildPatchSite result{};
    for (std::size_t index = 0; index < count; ++index) {
        const auto address = start + index * 4U;
        const std::uint32_t instruction = ReadInstruction(
            reinterpret_cast<const void *>(address));
        unsigned constant_register = 0;
        std::uint32_t constant = 0;
        if (!DecodeMovzX(
                instruction, &constant_register, &constant) ||
            constant_register != 16) {
            continue;
        }
        bool consumed = false;
        for (std::size_t following = index + 1;
             following < count && following <= index + 4;
             ++following) {
            unsigned destination = 0;
            unsigned left = 0;
            unsigned right = 0;
            if (DecodeMulX(
                    ReadInstruction(reinterpret_cast<const void *>(
                        start + following * 4U)),
                    &destination, &left, &right) &&
                (left == constant_register || right == constant_register)) {
                consumed = true;
                break;
            }
        }
        if (!consumed) continue;
        if (found) return false;
        found = true;
        result.address = reinterpret_cast<void *>(address);
        result.instruction = instruction;
        result.offset = static_cast<std::uint32_t>(index * 4U);
    }
    if (!found) {
        return false;
    }
    *site = result;
    return true;
}

enum class InstructionWriteResult {
    kFailed,
    kAlreadyReplacement,
    kChanged,
};

struct ExecutablePageBackup {
    void *page = nullptr;
    std::array<unsigned char, kExecutablePageSize> bytes{};
    std::array<unsigned char, kExecutablePageSize> installed{};
    bool captured = false;
    bool privatized = false;
};

bool CaptureExecutablePage(void *address, const AppImage &,
                           ExecutablePageBackup *backup) {
    // The address comes from symbol/operand resolution. Snapshot live memory;
    // do not require its mapping or bytes to still resemble the original APK.
    if (address == nullptr || backup == nullptr ||
        sysconf(_SC_PAGESIZE) != static_cast<long>(kExecutablePageSize)) return false;
    const auto page = reinterpret_cast<std::uintptr_t>(address) &
                      ~(kExecutablePageSize - 1U);
    std::memcpy(backup->bytes.data(), reinterpret_cast<const void *>(page),
                backup->bytes.size());
    backup->page = reinterpret_cast<void *>(page);
    backup->captured = true;
    return true;
}

bool RestoreExecutablePage(ExecutablePageBackup *backup) {
    if (backup == nullptr || !backup->captured || backup->page == nullptr) {
        return false;
    }
    std::array<unsigned char, kExecutablePageSize> restored_bytes{};
    std::memcpy(restored_bytes.data(), backup->page, kExecutablePageSize);
    // Reverse only our changed words. Preserve earlier and later hooks in the
    // same page, and do not overwrite a replacement installed at our own site.
    for (std::size_t i = 0; i < kExecutablePageSize; i += 4) {
        const auto *before = backup->bytes.data() + i;
        const auto *after = backup->installed.data() + i;
        auto *current = restored_bytes.data() + i;
        if (std::memcmp(before, after, 4) == 0) continue;
        if (std::memcmp(current, after, 4) != 0 &&
            std::memcmp(current, before, 4) != 0) {
            LOGE("page_restore ownership_changed page=%p offset=%zu", backup->page, i);
            return false;
        }
        std::memcpy(current, before, 4);
    }
    void *prepared = mmap(nullptr, kExecutablePageSize, PROT_READ | PROT_WRITE,
                          MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (prepared == MAP_FAILED) return false;
    std::memcpy(prepared, restored_bytes.data(), kExecutablePageSize);
    if (mprotect(prepared, kExecutablePageSize, PROT_READ | PROT_EXEC) != 0) {
        munmap(prepared, kExecutablePageSize);
        return false;
    }
    __builtin___clear_cache(static_cast<char *>(prepared),
                            static_cast<char *>(prepared) + kExecutablePageSize);
    void *restored = mremap(prepared, kExecutablePageSize, kExecutablePageSize,
                            MREMAP_MAYMOVE | MREMAP_FIXED, backup->page);
    if (restored != backup->page) {
        munmap(prepared, kExecutablePageSize);
        LOGE("page_restore failed page=%p errno=%d", backup->page, errno);
        return false;
    }
    __builtin___clear_cache(
        static_cast<char *>(backup->page),
        static_cast<char *>(backup->page) + kExecutablePageSize);
    const bool verified = std::memcmp(
        backup->page, restored_bytes.data(), restored_bytes.size()) == 0;
    if (!verified) {
        LOGE("page_restore readback_failed page=%p", backup->page);
        return false;
    }
    backup->privatized = false;
    return true;
}

void WriteAbsoluteBranch(void *destination, const void *target) {
    auto *bytes = static_cast<unsigned char *>(destination);
    const std::uint32_t instructions[] = {
        kLoadAbsoluteTarget,
        kBranchAbsoluteTarget,
    };
    const std::uintptr_t absolute =
        reinterpret_cast<std::uintptr_t>(target);
    std::memcpy(bytes, instructions, sizeof(instructions));
    std::memcpy(bytes + sizeof(instructions), &absolute, sizeof(absolute));
}

bool IsRelocatableEntry(const void *address) {
    if (address == nullptr ||
        (reinterpret_cast<std::uintptr_t>(address) & 3U) != 0U) {
        return false;
    }
    for (std::size_t offset = 0; offset < kInlineEntryReadbackSize;
         offset += sizeof(std::uint32_t)) {
        const std::uint32_t word = ReadInstruction(
            static_cast<const unsigned char *>(address) + offset);
        const bool branch_immediate =
            (word & 0x7c000000U) == 0x14000000U;
        const bool branch_conditional =
            (word & 0xff000010U) == 0x54000000U;
        const bool compare_branch =
            (word & 0x7e000000U) == 0x34000000U;
        const bool test_branch =
            (word & 0x7e000000U) == 0x36000000U;
        const bool address_relative =
            (word & 0x1f000000U) == 0x10000000U;
        const bool literal_load =
            (word & 0x3b000000U) == 0x18000000U;
        const bool branch_register =
            (word & 0xfe000000U) == 0xd6000000U;
        if (branch_immediate || branch_conditional || compare_branch ||
            test_branch || address_relative || literal_load ||
            branch_register) {
            LOGE(
                "inline_relocation refused address=%p offset=%zu word=0x%08x",
                address, offset, word);
            return false;
        }
    }
    return true;
}

// Chain a pre-existing terminal veneer instead of relocating PC-relative
// instructions verbatim. Preserve its scratch register and destination.
bool BuildOriginalEntry(const void *address, void *output) {
    const auto pc = reinterpret_cast<std::uintptr_t>(address);
    const auto *bytes = static_cast<const unsigned char *>(address);
    const auto a = ReadInstruction(bytes), b = ReadInstruction(bytes + 4);
    const auto c = ReadInstruction(bytes + 8);
    const unsigned reg = a & 31U;
    std::uintptr_t target = 0;
    bool veneer = false;
    if ((a & 0x9f000000U) == 0x90000000U && reg != 31 &&
        (b & 0xffc003ffU) == (0x91000000U | (reg << 5) | reg) &&
        c == (0xd61f0000U | (reg << 5))) {
        std::int64_t imm = ((a >> 5) & 0x7ffffU) << 2 | ((a >> 29) & 3U);
        if (imm & (1LL << 20)) imm -= (1LL << 21);
        target = (pc & ~std::uintptr_t{4095}) + imm * 4096 + ((b >> 10) & 4095U);
        veneer = true;
    } else if ((a & 0xffffffe0U) == 0x58000040U && reg != 31 &&
               b == (0xd61f0000U | (reg << 5))) {
        std::memcpy(&target, bytes + 8, sizeof(target));
        veneer = true;
    }
    if (veneer) {
        const std::uint32_t load = 0x58000040U | reg;
        const std::uint32_t branch = 0xd61f0000U | (reg << 5);
        std::memcpy(output, &load, 4);
        std::memcpy(static_cast<unsigned char *>(output) + 4, &branch, 4);
        std::memcpy(static_cast<unsigned char *>(output) + 8, &target, 8);
        return true;
    }
    if (!IsRelocatableEntry(address)) return false;
    std::memcpy(output, address, kInlineEntryReadbackSize);
    WriteAbsoluteBranch(static_cast<unsigned char *>(output) + 16,
                        reinterpret_cast<void *>(pc + 16));
    return true;
}

void RecordRollbackFailure(const char *stage);

bool InstallSelfInlineHook(void *address, void *replacement, void **original,
                           ExecutablePageBackup *backup, void **trampoline,
                           std::array<unsigned char,
                                      kInlineEntryReadbackSize> *installed) {
    if (address == nullptr || replacement == nullptr || original == nullptr ||
        backup == nullptr || !backup->captured || backup->privatized ||
        trampoline == nullptr || installed == nullptr) {
        return false;
    }
    const auto target = reinterpret_cast<std::uintptr_t>(address);
    const auto page = reinterpret_cast<std::uintptr_t>(backup->page);
    if (target < page ||
        target - page + kInlineEntryReadbackSize > kExecutablePageSize) {
        return false;
    }

    void *trampoline_page = mmap(
        nullptr, kExecutablePageSize, PROT_READ | PROT_WRITE,
        MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (trampoline_page == MAP_FAILED) return false;
    if (!BuildOriginalEntry(address, trampoline_page)) {
        munmap(trampoline_page, kExecutablePageSize);
        return false;
    }
    if (mprotect(
            trampoline_page, kExecutablePageSize,
            PROT_READ | PROT_EXEC) != 0) {
        munmap(trampoline_page, kExecutablePageSize);
        return false;
    }
    __builtin___clear_cache(
        static_cast<char *>(trampoline_page),
        static_cast<char *>(trampoline_page) +
            kInlineEntryReadbackSize * 2U);

    void *replacement_page = mmap(
        nullptr, kExecutablePageSize, PROT_READ | PROT_WRITE,
        MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (replacement_page == MAP_FAILED) {
        munmap(trampoline_page, kExecutablePageSize);
        return false;
    }
    std::memcpy(backup->bytes.data(), backup->page, kExecutablePageSize);
    std::memcpy(replacement_page, backup->bytes.data(), kExecutablePageSize);
    auto *replacement_entry = static_cast<unsigned char *>(replacement_page) +
        (target - page);
    WriteAbsoluteBranch(replacement_entry, replacement);
    std::memcpy(backup->installed.data(), replacement_page, kExecutablePageSize);
    std::memcpy(installed->data(), replacement_entry, installed->size());
    if (mprotect(
            replacement_page, kExecutablePageSize,
            PROT_READ | PROT_EXEC) != 0) {
        munmap(replacement_page, kExecutablePageSize);
        munmap(trampoline_page, kExecutablePageSize);
        return false;
    }
    __builtin___clear_cache(
        static_cast<char *>(replacement_page),
        static_cast<char *>(replacement_page) + kExecutablePageSize);
    // Publish the original path before target code can reach the replacement.
    // Retain this executable page until process exit, including after rollback:
    // a thread may already be executing it when the entry is restored.
    *original = trampoline_page;
    *trampoline = trampoline_page;
    g_write_started.store(true, std::memory_order_release);
    void *remapped = mremap(
        replacement_page, kExecutablePageSize, kExecutablePageSize,
        MREMAP_MAYMOVE | MREMAP_FIXED, backup->page);
    if (remapped != backup->page) {
        if (remapped == MAP_FAILED) {
            munmap(replacement_page, kExecutablePageSize);
        } else {
            munmap(remapped, kExecutablePageSize);
        }
        return false;
    }
    backup->privatized = true;
    __builtin___clear_cache(
        static_cast<char *>(backup->page),
        static_cast<char *>(backup->page) + kExecutablePageSize);

    const bool verified =
        std::memcmp(address, installed->data(), installed->size()) == 0;
    if (!verified) {
        const bool restored = RestoreExecutablePage(backup);
        if (!restored) RecordRollbackFailure("self_hook_verify");
        return false;
    }
    *original = trampoline_page;
    *trampoline = trampoline_page;
    LOGI(
        "self_hook installed address=%p replacement=%p trampoline=%p",
        address, replacement, trampoline_page);
    return true;
}

InstructionWriteResult WriteInstruction(void *address,
                                        std::uint32_t expected,
                                        std::uint32_t replacement,
                                        bool *changed = nullptr) {
    if (changed != nullptr) *changed = false;
    if (address == nullptr ||
        (reinterpret_cast<std::uintptr_t>(address) & 3U) != 0 ||
        pthread_mutex_lock(&g_instruction_patch_mutex) != 0) {
        return InstructionWriteResult::kFailed;
    }
    InstructionWriteResult result = InstructionWriteResult::kFailed;
    do {
        const std::uint32_t before = ReadInstruction(address);
        folder_diag_child_instruction_last = before;
        if (before == replacement) {
            result = InstructionWriteResult::kAlreadyReplacement;
            break;
        }
        if (before != expected) break;

        const long page_size_value = sysconf(_SC_PAGESIZE);
        if (page_size_value <= 0) break;
        const auto page_size = static_cast<std::size_t>(page_size_value);
        const auto instruction_address =
            reinterpret_cast<std::uintptr_t>(address);
        const auto page = instruction_address & ~(page_size - 1U);
        const auto page_offset = instruction_address - page;
        void *replacement_page = mmap(
            nullptr, page_size, PROT_READ | PROT_WRITE,
            MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
        if (replacement_page == MAP_FAILED) break;
        std::memcpy(
            replacement_page, reinterpret_cast<const void *>(page),
            page_size);
        auto *replacement_address = reinterpret_cast<std::uint32_t *>(
            static_cast<char *>(replacement_page) + page_offset);
        if (__atomic_load_n(replacement_address, __ATOMIC_ACQUIRE) != expected) {
            munmap(replacement_page, page_size);
            break;
        }
        __atomic_store_n(
            replacement_address, replacement, __ATOMIC_RELEASE);
        if (mprotect(
                replacement_page, page_size,
                PROT_READ | PROT_EXEC) != 0) {
            munmap(replacement_page, page_size);
            break;
        }
        __builtin___clear_cache(
            static_cast<char *>(replacement_page),
            static_cast<char *>(replacement_page) + page_size);
        void *remapped = mremap(
            replacement_page, page_size, page_size,
            MREMAP_MAYMOVE | MREMAP_FIXED,
            reinterpret_cast<void *>(page));
        if (remapped != reinterpret_cast<void *>(page)) {
            if (remapped == MAP_FAILED) {
                munmap(replacement_page, page_size);
            } else {
                munmap(remapped, page_size);
            }
            break;
        }
        if (changed != nullptr) *changed = true;
        __builtin___clear_cache(
            reinterpret_cast<char *>(page),
            reinterpret_cast<char *>(page + page_size));
        const std::uint32_t after = ReadInstruction(address);
        folder_diag_child_instruction_last = after;
        if (after != replacement) {
            LOGE(
                "instruction_patch readback_failed address=%p actual=0x%08x",
                address, after);
            break;
        }
        result = InstructionWriteResult::kChanged;
    } while (false);
    pthread_mutex_unlock(&g_instruction_patch_mutex);
    return result;
}

struct HookSpec {
    const char *name;
    ResolvedSymbol symbol;
    void *replacement;
    void **original;
    void *trampoline = nullptr;
    std::array<unsigned char, kInlineEntryReadbackSize> installed_entry{};
    ExecutablePageBackup page_backup{};
};

template <std::size_t Size>
bool RollbackInlineHooks(std::array<HookSpec, Size> *hooks,
                         std::size_t count) {
    if (hooks == nullptr || count > hooks->size()) return false;
    bool complete = true;
    for (std::size_t index = count; index > 0; --index) {
        HookSpec &hook = (*hooks)[index - 1U];
        if (hook.page_backup.privatized) {
            complete = RestoreExecutablePage(&hook.page_backup) && complete;
        }
        // Keep published continuations valid for in-flight calls and for any
        // entry whose restoration failed. The worker never retries writes.
        LOGW("hook_rollback name=%s complete=%d", hook.name, complete);
    }
    return complete;
}

void SetContractActive(bool active) {
    const std::uint32_t value = active ? 1U : 0U;
    __atomic_store_n(&hc_contract_active, value, __ATOMIC_RELEASE);
    __atomic_store_n(
        &folder_diag_contract_active,
        static_cast<std::uint64_t>(value), __ATOMIC_RELEASE);
}

ResolvedSymbol ResolveProfileSymbol(const AppImage &image,
                                    std::uintptr_t offset,
                                    std::size_t size,
                                    const char *name) {
    if (image.base == 0 || image.size == 0 || size == 0 ||
        offset >= image.size || size > image.size - offset ||
        image.base > UINTPTR_MAX - offset) {
        LOGE(
            "symbol_profile refused name=%s offset=0x%" PRIxPTR
            " size=%zu image_size=0x%zx",
            name, offset, size, image.size);
        return {};
    }
    ResolvedSymbol result{
        reinterpret_cast<void *>(image.base + offset),
        size,
    };
    LOGI(
        "symbol_profile name=%s address=%p offset=0x%" PRIxPTR " size=%zu",
        name, result.address, offset, size);
    return result;
}

void RecordRollbackFailure(const char *stage) {
    SetContractActive(false);
    LOGE("rollback_unrecoverable stage=%s pid=%d", stage, getpid());
    g_rollback_incomplete.store(true, std::memory_order_release);
    LOGE("rollback_incomplete: hooks inactive, retained continuations, no retry");
}

// Current-version layout transaction. All sites are validated and both pages
// are captured before the existing animation/child hooks write any bytes.
struct CompactLayoutPlan {
    bool required = false;
    ExecutablePageBackup height_page{};
    ExecutablePageBackup spacing_page{};
    ExecutablePageBackup drag_window_page{};
    std::array<unsigned char, kExecutablePageSize> drag_window_after{};
    ExecutablePageBackup grid_clip_page{};
    std::array<unsigned char, kExecutablePageSize> grid_clip_after{};
    ExecutablePageBackup marker_page{};
    std::array<unsigned char, kExecutablePageSize> marker_after{};
    std::array<ExecutablePageBackup, 4> density_pages{};
    std::array<std::array<unsigned char, kExecutablePageSize>, 4> density_after{};
    std::array<unsigned char, kExecutablePageSize> height_after{};
    std::array<unsigned char, kExecutablePageSize> spacing_after{};
};

bool PrepareCompactLayout(const AppImage &image, CompactLayoutPlan *plan) {
    if (plan == nullptr) return false;
    plan->required = true;
    // Try every build by structure. Version labels and Build IDs are diagnostic
    // information, not an eligibility whitelist for the layout policy.
    if (image.executable_offset >= image.size || image.executable_size == 0 ||
        image.executable_size > image.size - image.executable_offset ||
        image.base > UINTPTR_MAX - image.executable_offset) return false;
    const auto *text = reinterpret_cast<const void *>(image.base + image.executable_offset);
    constexpr std::array<MaskedInstruction, 7> limits{{
        {0xd2800000U, 0xffe0001fU}, {0x14000006U, 0xffffffffU},
        {0xd2800000U, 0xffe0001fU}, {0x14000004U, 0xffffffffU},
        {0xf85f83a1U, 0xffffffffU}, {0xfc5b83a0U, 0xffffffffU},
        {0xd2800000U, 0xffe0001fU},
    }};
    constexpr std::array<MaskedInstruction, 4> epilogue{{
        {0xaa1603e0U, 0xffffffffU}, {0xaa1d03efU, 0xffffffffU},
        {0xa8c179fdU, 0xffffffffU}, {0xd65f03c0U, 0xffffffffU},
    }};
    void *limit_address = nullptr;
    void *spacing_address = nullptr;
    // Sparse structural signature: preserve receiver/frame and all spacing
    // stores; allow relocations, pool slots and branch call targets to move.
    std::array<MaskedInstruction, 0x12c / 4> spacing_shape{};
    const auto expect = [&spacing_shape](std::size_t offset, std::uint32_t word) {
        spacing_shape[offset / 4] = {word, 0xffffffffU};
    };
    expect(0, 0xa9bf79fdU);
    expect(4, 0xaa0f03fdU);
    expect(8, 0xd10041efU);
    expect(0xc, 0xf81f83a1U);
    expect(0x5c, 0xf804301fU);
    expect(0x60, 0xf803b01fU);
    expect(0xd8, 0xfc043001U);
    expect(0xdc, 0xfc03b000U);
    expect(0xfc, 0xf804303fU);
    expect(0x100, 0xfc03b020U);
    expect(0x114, 0xfc043020U);
    expect(0x118, 0xfc03b020U);
    for (std::size_t i = 0; i < epilogue.size(); ++i)
        expect(0x11c + i * 4, epilogue[i].expected);
    if (!FindUniqueMaskedPattern(text, image.executable_size, spacing_shape, 0,
                                  &spacing_address) ||
        !FindUniqueMaskedPattern(text, image.executable_size, limits, 0,
                                  &limit_address)) return false;
    // calItemSpacing has an intentional early zero-spacing return as well as
    // the shared nonzero return. Only the final epilogue is replaced.
    auto *exit = static_cast<unsigned char *>(spacing_address) + 0x11c;
    if (!CaptureExecutablePage(limit_address, image, &plan->height_page) ||
        !CaptureExecutablePage(exit, image, &plan->spacing_page)) return false;
    const auto height_offset = reinterpret_cast<std::uintptr_t>(limit_address) -
        reinterpret_cast<std::uintptr_t>(plan->height_page.page);
    const auto spacing_offset = reinterpret_cast<std::uintptr_t>(exit) -
        reinterpret_cast<std::uintptr_t>(plan->spacing_page.page);
    if (height_offset + 28 > kExecutablePageSize ||
        spacing_offset + 16 > kExecutablePageSize) return false;
    plan->height_after = plan->height_page.bytes;
    plan->spacing_after = plan->spacing_page.bytes;
    const std::uint32_t eight = 0xd2800000U | (g_config.rows << 5);
    for (const auto relative : {0U, 8U, 24U}) {
        std::memcpy(plan->height_after.data() + height_offset + relative,
                    &eight, sizeof(eight));
    }
    WriteAbsoluteBranch(plan->spacing_after.data() + spacing_offset,
                        reinterpret_cast<void *>(hc_compact_spacing_exit));
    LOGI("layout_plan verified rows=%u gap=%u height=%p exit=%p",
         g_config.rows, g_config.vertical_gap, limit_address, exit);
    return true;
}

bool CommitLayoutPage(ExecutablePageBackup *backup,
                     const std::array<unsigned char, kExecutablePageSize> &after) {
    if (backup == nullptr || !backup->captured || backup->privatized)
        return false;
    auto merged = after;
    std::array<unsigned char, kExecutablePageSize> current{};
    std::memcpy(current.data(), backup->page, kExecutablePageSize);
    for (std::size_t i = 0; i < kExecutablePageSize; i += 4) {
        if (std::memcmp(backup->bytes.data() + i, after.data() + i, 4) == 0)
            std::memcpy(merged.data() + i, current.data() + i, 4);
    }
    void *prepared = mmap(nullptr, kExecutablePageSize, PROT_READ | PROT_WRITE,
                          MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (prepared == MAP_FAILED) return false;
    std::memcpy(prepared, merged.data(), kExecutablePageSize);
    if (mprotect(prepared, kExecutablePageSize, PROT_READ | PROT_EXEC) != 0) {
        munmap(prepared, kExecutablePageSize);
        return false;
    }
    __builtin___clear_cache(static_cast<char *>(prepared),
                            static_cast<char *>(prepared) + kExecutablePageSize);
    backup->bytes = current;
    backup->installed = merged;
    void *result = mremap(prepared, kExecutablePageSize, kExecutablePageSize,
                          MREMAP_MAYMOVE | MREMAP_FIXED, backup->page);
    if (result == MAP_FAILED) {
        munmap(prepared, kExecutablePageSize);
        return false;
    }
    backup->privatized = true;
    __builtin___clear_cache(static_cast<char *>(backup->page),
                            static_cast<char *>(backup->page) + kExecutablePageSize);
    return result == backup->page &&
        std::memcmp(backup->page, merged.data(), kExecutablePageSize) == 0;
}

// A failed return anywhere in installation reverses completed steps. No
// version, original-page or other-module eligibility policy lives here.
template <std::size_t HookCount>
struct FolderInstallTransaction {
    CompactLayoutPlan &layout;
    std::array<HookSpec, HookCount> &hooks;
    ChildPatchSite &child;
    bool child_attempted = false;
    bool committed = false;

    ~FolderInstallTransaction() {
        if (committed) return;
        SetContractActive(false);
        bool complete = true;
        if (layout.marker_page.privatized) complete = RestoreExecutablePage(&layout.marker_page) && complete;
        if (layout.drag_window_page.privatized) complete = RestoreExecutablePage(&layout.drag_window_page) && complete;
        if (layout.grid_clip_page.privatized) complete = RestoreExecutablePage(&layout.grid_clip_page) && complete;
        for (std::size_t i = layout.density_pages.size(); i > 0; --i) {
            auto &page = layout.density_pages[i - 1];
            if (page.privatized) complete = RestoreExecutablePage(&page) && complete;
        }
        for (auto *page : {&layout.height_page, &layout.spacing_page}) {
            if (page->privatized) complete = RestoreExecutablePage(page) && complete;
        }
        if (child_attempted) {
            const bool restored = WriteInstruction(child.address,
                kTargetChildInstruction, child.instruction) != InstructionWriteResult::kFailed;
            if (restored) {
                g_child_patch_owned.store(false, std::memory_order_release);
                g_child_instruction_address = nullptr;
            }
            complete = restored && complete;
        }
        complete = RollbackInlineHooks(&hooks, hooks.size()) && complete;
        if (!complete) RecordRollbackFailure("install_transaction");
        LOGW("install_transaction reversed complete=%d", complete);
    }
};

bool ResolveDensitySymbols(const AppImage &image, ResolvedSymbol *calibrate,
                           ResolvedSymbol *cell) {
    constexpr std::array<MaskedInstruction, 9> calibration{{
        {0xa9bf79fdU, 0xffffffffU}, {0xaa0f03fdU, 0xffffffffU},
        {0xd10081efU, 0xffffffffU}, {0xf81f83a1U, 0xffffffffU},
        {0xd2800081U, 0xffffffffU}, {0x94000000U, 0xfc000000U},
        {0xf85f83a1U, 0xffffffffU}, {0xf81f03a0U, 0xffffffffU},
        {0xb800f001U, 0xffffffffU},
    }};
    // Scan the body: an existing entry hook does not hide this getter.
    constexpr std::array<MaskedInstruction, 8> getter{{
        {0xaa0003e1U, 0xffffffffU}, {0x94000000U, 0xfc000000U},
        {0xb8473001U, 0xffffffffU}, {0x8b1c8021U, 0xffffffffU},
        {0xfc407020U, 0xffffffffU}, {0xaa1d03efU, 0xffffffffU},
        {0xa8c179fdU, 0xffffffffU}, {0xd65f03c0U, 0xffffffffU},
    }};
    auto *text = reinterpret_cast<void *>(image.base + image.executable_offset);
    void *body = nullptr;
    if (!FindUniqueMaskedPattern(text, image.executable_size, calibration, 0,
                                 &calibrate->address) ||
        !FindUniqueMaskedPattern(text, image.executable_size, getter, 0, &body))
        return false;
    constexpr std::array<MaskedInstruction, 4> exit{{
        {0xfc407020U, 0xffffffffU}, {0xaa1d03efU, 0xffffffffU},
        {0xa8c179fdU, 0xffffffffU}, {0xd65f03c0U, 0xffffffffU},
    }};
    void *calibrate_exit = nullptr;
    const auto start_offset = reinterpret_cast<std::uintptr_t>(calibrate->address) -
                              reinterpret_cast<std::uintptr_t>(text);
    const auto available = image.executable_size - start_offset;
    const auto scan_size = available < 0x3ec ? available : 0x3ec;
    if (!FindUniqueMaskedPattern(calibrate->address, scan_size, exit, 0, &calibrate_exit))
        return false;
    calibrate->address = calibrate_exit;
    calibrate->size = 16;
    cell->address = static_cast<unsigned char *>(body) + 16;
    cell->size = 16;
    return true;
}

// These two animation paths read the unscaled configuration directly, unlike
// the scrollable grid. Keep their height units consistent with the grid.
bool ResolveAnimationDensity(const AppImage &image, ResolvedSymbol *cell,
                             ResolvedSymbol *cache) {
    constexpr std::array<MaskedInstruction, 10> cell_shape{{
        {0xa9bf79fdU, 0xffffffffU}, {0xaa0f03fdU, 0xffffffffU},
        {0xaa0103e0U, 0xffffffffU}, {0x94000000U, 0xfc000000U},
        {0xb8473001U, 0xffffffffU}, {0x8b1c8021U, 0xffffffffU},
        {0xfc407020U, 0xffffffffU}, {0xaa1d03efU, 0xffffffffU},
        {0xa8c179fdU, 0xffffffffU}, {0xd65f03c0U, 0xffffffffU},
    }};
    constexpr std::array<MaskedInstruction, 16> cache_shape{{
        {0xb8473001U, 0xffffffffU}, {0x8b1c8021U, 0xffffffffU},
        {0xfc407020U, 0xffffffffU}, {0xa9460740U, 0xffffffffU},
        {0x91004000U, 0xffffffffU}, {0xeb00003fU, 0xffffffffU},
        {0x54000009U, 0xff00001fU}, {0xf9003340U, 0xffffffffU},
        {0xd1003c00U, 0xffffffffU}, {0, 0}, {0, 0},
        {0xf81ff001U, 0xffffffffU}, {0xfc007000U, 0xffffffffU},
        {0xf85f83a2U, 0xffffffffU}, {0xd2802471U, 0xffffffffU},
        {0xb8316840U, 0xffffffffU},
    }};
    auto *text = reinterpret_cast<void *>(image.base + image.executable_offset);
    void *getter = nullptr, *cache_body = nullptr;
    if (!FindUniqueMaskedPattern(text, image.executable_size, cell_shape, 0, &getter) ||
        !FindUniqueMaskedPattern(text, image.executable_size, cache_shape, 0, &cache_body))
        return false;
    *cell = {static_cast<unsigned char *>(getter) + 24, 16};
    *cache = {static_cast<unsigned char *>(cache_body) + 8, 16};
    return true;
}

bool PrepareNoNewInstallMarker(const AppImage &image, CompactLayoutPlan *layout) {
    // getPrefixAssetName's new-install branch. The preceding other-prefix
    // branch remains intact. Return its existing empty string, not null.
    constexpr std::array<MaskedInstruction, 17> shape{{
        {0xd2802971U, 0xffffffffU}, {0xb8716801U, 0xffffffffU},
        {0x8b1c8021U, 0xffffffffU}, {0xf85ff020U, 0xffffffffU},
        {0xd34c7c00U, 0xffffffffU}, {0xd13dc01eU, 0xffffffffU},
        {0xf87e7abeU, 0xffffffffU}, {0xd63f03c0U, 0xffffffffU},
        {0x37200080U, 0xffffffffU}, {0x9141f761U, 0xffffffffU},
        {0xf941f421U, 0xffffffffU}, {0x14000002U, 0xffffffffU},
        {0xf9407f61U, 0xffffffffU}, {0xaa0103e0U, 0xffffffffU},
        {0xaa1d03efU, 0xffffffffU}, {0xa8c179fdU, 0xffffffffU},
        {0xd65f03c0U, 0xffffffffU},
    }};
    void *body = nullptr;
    if (!FindUniqueMaskedPattern(reinterpret_cast<void *>(image.base + image.executable_offset),
        image.executable_size, shape, 0, &body)) return false;
    auto *site = static_cast<unsigned char *>(body) + 32;
    if (!CaptureExecutablePage(site, image, &layout->marker_page)) return false;
    const auto offset = reinterpret_cast<std::uintptr_t>(site) -
                        reinterpret_cast<std::uintptr_t>(layout->marker_page.page);
    if (offset + 4 > kExecutablePageSize) return false;
    layout->marker_after = layout->marker_page.bytes;
    constexpr std::uint32_t skip_marker = 0x14000004U; // B to existing no-marker arm
    std::memcpy(layout->marker_after.data() + offset, &skip_marker, 4);
    return true;
}

bool PrepareGridDefaultClip(const AppImage &image, CompactLayoutPlan *layout) {
    constexpr std::array<MaskedInstruction, 17> shape{{
        {0x91407370U, 0xffffffffU}, {0xf9472e10U, 0xffffffffU},
        {0xf94c7b7eU, 0xffffffffU}, {0xa901c1feU, 0xffffffffU},
        {0xf85f83b0U, 0xffffffffU}, {0x9141cb7eU, 0xffffffffU},
        {0xf9410bdeU, 0xffffffffU}, {0xa900c1feU, 0xffffffffU},
        {0xf85b83b0U, 0xffffffffU}, {0xf90001f0U, 0xffffffffU},
        {0xaa0003e1U, 0xffffffffU}, {0xf85d03a2U, 0xffffffffU},
        {0xf85c83a3U, 0xffffffffU}, {0xf85d83a5U, 0xffffffffU},
        {0x9143b764U, 0xffffffffU}, {0xf9458884U, 0xffffffffU},
        {0x94000000U, 0xfc000000U},
    }};
    void *body = nullptr;
    const auto begin = image.base + image.executable_offset;
    const auto end = begin + image.executable_size;
    if (!FindUniqueMaskedPattern(reinterpret_cast<void *>(begin), image.executable_size,
                                  shape, 0, &body)) return false;
    const auto call = reinterpret_cast<std::uintptr_t>(body) + 64;
    std::uintptr_t constructor = 0;
    if (!DecodeBlTarget(call, ReadInstruction(reinterpret_cast<void *>(call)), &constructor) ||
        constructor < begin || constructor > end || end - constructor < 0x64) return false;
    // The constructor's own default path loads the default enum through x7.
    // Preserve its pool offsets, changing only destination/base registers to x16.
    const auto add = ReadInstruction(reinterpret_cast<void *>(constructor + 0x58));
    const auto load = ReadInstruction(reinterpret_cast<void *>(constructor + 0x5c));
    if ((add & 0xffc003ffU) != 0x91400367U ||
        (load & 0xffc003ffU) != 0xf94000e7U ||
        ReadInstruction(reinterpret_cast<void *>(constructor + 0x60)) != 0xd2800006U)
        return false;
    if (!CaptureExecutablePage(body, image, &layout->grid_clip_page)) return false;
    const auto offset = reinterpret_cast<std::uintptr_t>(body) -
                        reinterpret_cast<std::uintptr_t>(layout->grid_clip_page.page);
    if (offset + 8 > kExecutablePageSize) return false;
    layout->grid_clip_after = layout->grid_clip_page.bytes;
    const std::uint32_t words[]{(add & ~31U) | 16U,
                               (load & ~1023U) | (16U << 5) | 16U};
    std::memcpy(layout->grid_clip_after.data() + offset, words, sizeof(words));
    return true;
}

bool PrepareDragWindow(const AppImage &image, CompactLayoutPlan *layout) {
    // firstVisibleIndex -> columns -> columns * windowRows. Accept a foreign
    // row value; the surrounding data flow identifies the operand, not 6.
    constexpr std::array<MaskedInstruction, 16> shape{{
        {0x94000000U, 0xfc000000U}, {0xf81d83a0U, 0xffffffffU},
        {0xf9400340U, 0xffc003ffU}, {0xf9400000U, 0xffc003ffU},
        {0xf9402370U, 0xffffffffU}, {0x6b10001fU, 0xffffffffU},
        {0x54000081U, 0xffffffffU}, {0x91400362U, 0xffc003ffU},
        {0xf9400042U, 0xffc003ffU}, {0x94000000U, 0xfc000000U},
        {0x93417c01U, 0xffffffffU}, {0xd2800010U, 0xffe0001fU},
        {0x9b107c20U, 0xffffffffU}, {0xf85e83a2U, 0xffffffffU},
        {0xf81d03a0U, 0xffffffffU}, {0xb841f041U, 0xffffffffU},
    }};
    void *body = nullptr;
    if (!FindUniqueMaskedPattern(reinterpret_cast<void *>(image.base + image.executable_offset),
                                  image.executable_size, shape, 0, &body)) return false;
    auto *site = static_cast<unsigned char *>(body) + 44;
    if (!CaptureExecutablePage(site, image, &layout->drag_window_page)) return false;
    const auto offset = reinterpret_cast<std::uintptr_t>(site) -
                        reinterpret_cast<std::uintptr_t>(layout->drag_window_page.page);
    if (offset + 4 > kExecutablePageSize) return false;
    layout->drag_window_after = layout->drag_window_page.bytes;
    const std::uint32_t target = 0xd2800010U | (g_config.rows << 5); // MOV x16,#8; same window as rendering.
    std::memcpy(layout->drag_window_after.data() + offset, &target, sizeof(target));
    return true;
}

bool InstallFolderContract(const AppImage &image) {
    if (!g_configured.load(std::memory_order_acquire)) return false;
    if (!g_config.enabled) {
        if (!g_config.hide_new_marker) return false;
        CompactLayoutPlan marker{};
        if (!PrepareNoNewInstallMarker(image, &marker)) return false;
        // A marker-only attempt owns the same no-retry boundary as layout.
        // Even a failed rollback must never be followed by a fresh backup.
        g_write_started.store(true, std::memory_order_release);
        if (!CommitLayoutPage(&marker.marker_page, marker.marker_after)) {
            if (marker.marker_page.privatized && !RestoreExecutablePage(&marker.marker_page))
                g_rollback_incomplete.store(true);
            return false;
        }
        g_contract_committed.store(true, std::memory_order_release);
        return true;
    }
    SetContractActive(false);
    folder_diag_pool_invalid_last = 0;
    CompactLayoutPlan layout{};
    if (!PrepareCompactLayout(image, &layout) ||
        (g_config.hide_new_marker && !PrepareNoNewInstallMarker(image, &layout)) ||
        !PrepareGridDefaultClip(image, &layout) ||
        !PrepareDragWindow(image, &layout)) {
        LOGE("layout_plan refused; no hooks installed");
        return false;
    }

    ResolvedSymbol setup_view{};
    ResolvedSymbol row_num{};
    ResolvedSymbol get_child_count{};
    if (image.optimistic || image.profile == nullptr) {
        if (!ResolveSemanticSymbols(
                image, &setup_view, &row_num, &get_child_count)) {
            return false;
        }
    } else {
        setup_view = ResolveProfileSymbol(
            image, image.profile->setup_view_offset,
            image.profile->setup_view_size, kSetupViewSymbol);
        row_num = ResolveProfileSymbol(
            image, image.profile->row_num_offset,
            image.profile->row_num_size, kRowNumSymbol);
        get_child_count = ResolveProfileSymbol(
            image, image.profile->get_child_count_offset,
            image.profile->get_child_count_size, kGetChildCountSymbol);
    }

    std::uint32_t pool_offset = 0;
    std::uint32_t columns_slot = 0;
    ChildPatchSite child{};
    if ((!DeriveSetupLayout(setup_view, row_num, &pool_offset, &columns_slot) &&
         !DeriveOriginalSetupLayout(image, setup_view, row_num, &pool_offset, &columns_slot)) ||
        !DeriveChildPatchSite(get_child_count, &child)) {
        LOGE(
            "semantic_guard failed pool=0x%x slot=0x%x "
            "child_offset=0x%x child_word=0x%08x",
            pool_offset, columns_slot, child.offset, child.instruction);
        return false;
    }
    hc_pool_base_offset = pool_offset;
    hc_columns_slot_offset = columns_slot;
    folder_diag_child_patch_offset = child.offset;
    folder_diag_child_instruction_last = child.instruction;
    LOGI(
        "semantic_guard passed pool=0x%x columns_slot=0x%x "
        "buffer_rows=%u child_offset=0x%x child_word=0x%08x",
        pool_offset, columns_slot, hc_buffer_rows,
        child.offset, child.instruction);

    ResolvedSymbol calibrate{}, cell_height{};
    if (!ResolveDensitySymbols(image, &calibrate, &cell_height)) return false;
    ResolvedSymbol animation_cell{}, animation_cache{};
    if (!ResolveAnimationDensity(image, &animation_cell, &animation_cache)) return false;
    const std::array<ResolvedSymbol, 4> density_sites{
        calibrate, cell_height, animation_cell, animation_cache};
    hc_density_cache_resume = static_cast<unsigned char *>(animation_cache.address) + 16;
    for (std::size_t i = 0; i < density_sites.size(); ++i) {
        auto &page = layout.density_pages[i];
        if (!CaptureExecutablePage(density_sites[i].address, image, &page)) return false;
        const auto offset = reinterpret_cast<std::uintptr_t>(density_sites[i].address) -
                            reinterpret_cast<std::uintptr_t>(page.page);
        if (offset + 16 > kExecutablePageSize) return false;
        layout.density_after[i] = page.bytes;
        WriteAbsoluteBranch(layout.density_after[i].data() + offset,
                            reinterpret_cast<void *>(i == 3 ? hc_density_cache : hc_density_exit));
    }
    std::array<HookSpec, 2> hooks{{
        {
            kRowNumSymbol,
            row_num,
            reinterpret_cast<void *>(hc_row_num_hook),
            &hc_row_num_original,
        },
        {
            kSetupViewSymbol,
            setup_view,
            reinterpret_cast<void *>(hc_setup_view_hook),
            &hc_setup_view_original,
        },
    }};
    FolderInstallTransaction transaction{layout, hooks, child};
    for (HookSpec &hook : hooks) {
        if (!CaptureExecutablePage(hook.symbol.address, image, &hook.page_backup) ||
            !InstallSelfInlineHook(hook.symbol.address, hook.replacement,
                hook.original, &hook.page_backup, &hook.trampoline, &hook.installed_entry)) {
            LOGE("hook_install failed name=%s", hook.name);
            return false;
        }
    }

    // An already-satisfied result is success, not a conflict. Only changes
    // performed by this transaction belong to its reverse operation.
    g_child_instruction_address = child.address;
    const auto child_write = WriteInstruction(child.address, child.instruction,
        kTargetChildInstruction, &transaction.child_attempted);
    if (child_write == InstructionWriteResult::kFailed) return false;
    g_child_patch_owned.store(transaction.child_attempted, std::memory_order_release);
    if (!CommitLayoutPage(&layout.spacing_page, layout.spacing_after) ||
        !CommitLayoutPage(&layout.height_page, layout.height_after)) return false;

    for (std::size_t i = 0; i < layout.density_pages.size(); ++i)
        if (!CommitLayoutPage(&layout.density_pages[i], layout.density_after[i])) return false;

    if (!CommitLayoutPage(&layout.grid_clip_page, layout.grid_clip_after) ||
        !CommitLayoutPage(&layout.drag_window_page, layout.drag_window_after) ||
        (g_config.hide_new_marker && !CommitLayoutPage(&layout.marker_page, layout.marker_after))) return false;

    // Check the result once, after the attempt; failure uses the same reverse.
    if (ReadInstruction(child.address) != kTargetChildInstruction) return false;
    for (const HookSpec &hook : hooks) {
        if (std::memcmp(hook.symbol.address, hook.installed_entry.data(),
                        hook.installed_entry.size()) != 0) return false;
    }
    transaction.committed = true;
    SetContractActive(true);
    g_contract_committed.store(true, std::memory_order_release);
    LOGI(
        "folder_contract active build=%s pid=%d base=%p build_id=%s "
        "configured layout committed",
        kModuleBuild, getpid(), reinterpret_cast<void *>(image.base),
        image.build_id);
    return true;
}

void *TargetWorker(void *) {
    constexpr timespec delay{0, 100 * 1000 * 1000};
    for (unsigned attempt = 0; attempt < 300; ++attempt) {
        char cmdline[256]{};
        if (!IsTargetProcess(cmdline, sizeof(cmdline))) {
            LOGW(
                "target_worker process_changed attempt=%u cmdline=%s",
                attempt, cmdline[0] != '\0' ? cmdline : "<unreadable>");
            g_worker_started.store(false, std::memory_order_release);
            return nullptr;
        }
        const AppImages images = FindAppImages(attempt == 0 || attempt == 299);
        if (images.count != 0) {
            for (std::size_t candidate = 0;
                 candidate < images.count; ++candidate) {
                const AppImage &image = images.items[candidate];
                const bool active = InstallFolderContract(image);
                if (active) {
                    LOGI(
                        "target_worker complete attempt=%u candidate=%zu "
                        "active=1 path=%s",
                        attempt, candidate, image.path);

                    return nullptr;
                }
                if (g_write_started.load(std::memory_order_acquire) ||
                    g_rollback_incomplete.load(std::memory_order_acquire)) {
                    LOGE("target_worker stopped_after_failed_install rollback_incomplete=%d",
                         g_rollback_incomplete.load(std::memory_order_acquire));
                    return nullptr;
                }
                LOGW(
                    "target_worker candidate_refused attempt=%u candidate=%zu "
                    "build_id=%s path=%s",
                    attempt, candidate, image.build_id, image.path);
            }
            LOGE(
                "target_worker no_compatible_candidate attempt=%u count=%zu",
                attempt, images.count);
            nanosleep(&delay, nullptr);
            continue;
        }
        nanosleep(&delay, nullptr);
    }
    LOGE("target_worker timeout waiting_for_supported_libapp pid=%d", getpid());
    return nullptr;
}

void MaybeStartTargetWorker(const char *reason) {
    if (!g_configured.load(std::memory_order_acquire) || (!g_config.enabled && !g_config.hide_new_marker)) return;
    if (g_contract_committed.load(std::memory_order_acquire)) return;
    if (g_write_started.load(std::memory_order_acquire) ||
        g_rollback_incomplete.load(std::memory_order_acquire)) return;
    char cmdline[256]{};
    if (!IsTargetProcess(cmdline, sizeof(cmdline))) {
        LOGD(
            "worker_gate skip reason=%s pid=%d cmdline=%s",
            reason, getpid(), cmdline[0] != '\0' ? cmdline : "<unreadable>");
        return;
    }
    if (g_worker_started.exchange(true, std::memory_order_acq_rel)) return;
    pthread_t worker{};
    const int result = pthread_create(&worker, nullptr, TargetWorker, nullptr);
    if (result == 0) {
        pthread_detach(worker);
        LOGI(
            "worker_gate started reason=%s pid=%d cmdline=%s",
            reason, getpid(), cmdline);
    } else {
        g_worker_started.store(false, std::memory_order_release);
        LOGE("worker_gate start_failed reason=%s result=%d", reason, result);
    }
}

__attribute__((constructor)) void OnNativeLibraryLoaded() {
    char cmdline[256]{};
    ReadProcText("/proc/self/cmdline", cmdline, sizeof(cmdline));
    LOGI(
        "constructor ready build=%s pid=%d cmdline=%s",
        kModuleBuild, getpid(),
        cmdline[0] != '\0' ? cmdline : "<unreadable>");
    StartFolderConfigTransport();
    MaybeStartTargetWorker("constructor");
}

void OnLibraryLoaded(const char *name, void *) {
    if (name != nullptr &&
        (std::strstr(name, "libapp.so") != nullptr ||
         HasApkSuffix(name))) {
        LOGD("native_load observed name=%s pid=%d", name, getpid());
    }
    MaybeStartTargetWorker("native_load");
}

}  // namespace

extern "C" __attribute__((visibility("default"), used))
NativeOnModuleLoaded native_init(const NativeAPIEntries *entries) {
    if (entries == nullptr || entries->version < 2 ||
        entries->hookFunc == nullptr || entries->unhookFunc == nullptr) {
        LOGE(
            "native_init rejected api=%p version=%u hook=%p unhook=%p",
            entries, entries != nullptr ? entries->version : 0,
            entries != nullptr ? reinterpret_cast<void *>(entries->hookFunc)
                               : nullptr,
            entries != nullptr ? reinterpret_cast<void *>(entries->unhookFunc)
                               : nullptr);
        return nullptr;
    }
    char cmdline[256]{};
    ReadProcText("/proc/self/cmdline", cmdline, sizeof(cmdline));
    LOGI(
        "native_init ready build=%s api=%u pid=%d cmdline=%s",
        kModuleBuild, entries->version, getpid(),
        cmdline[0] != '\0' ? cmdline : "<unreadable>");
    StartFolderConfigTransport();
    MaybeStartTargetWorker("native_init");
    return OnLibraryLoaded;
}

extern "C" __attribute__((visibility("default"), used))
bool pengeek_folder_configure(const FolderConfig *config) {
    if (!config || !ValidFolderConfig(*config)) return false;
    pthread_mutex_lock(&g_config_mutex);
    if (g_configured.load(std::memory_order_relaxed)) {
        pthread_mutex_unlock(&g_config_mutex);
        return false;
    }
    g_config = *config;
    hc_buffer_rows = config->rows;
    hc_columns_smi = config->columns * 2;
    hc_vertical_gap = config->vertical_gap;
    hc_density_ratio = config->density_per_mille / 1000.0;
    kTargetChildInstruction = 0xd2800010U | (config->rows << 5);
    g_configured.store(true, std::memory_order_release);
    pthread_mutex_unlock(&g_config_mutex);
    MaybeStartTargetWorker("configured");
    return true;
}
