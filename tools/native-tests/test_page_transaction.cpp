// Run the actual mmap/mremap transaction on Linux, without a Launcher or device.
#include "../../app/src/main/cpp/folder_module.cpp"
#include <cassert>
#include <string>

void StartFolderConfigTransport() {}
extern "C" void hc_row_num_hook() {}
extern "C" void hc_setup_view_hook() {}
extern "C" void hc_compact_spacing_exit() {}
extern "C" void hc_density_exit() {}
extern "C" void hc_density_cache() {}
extern "C" void hc_viewport_direct_exit() {}
extern "C" void hc_viewport_capped_exit() {}

bool fail_next_protection = false;
int protections_until_failure = -1;
unsigned char *foreign_child_page = nullptr;
int protections_until_foreign_child = -1;
std::uint32_t foreign_child_value = kTargetChildInstruction;
extern "C" int __real_mprotect(void *, std::size_t, int);
extern "C" int __wrap_mprotect(void *address, std::size_t size, int flags) {
    if (fail_next_protection || protections_until_failure == 0) {
        fail_next_protection = false;
        protections_until_failure = -1;
        errno = ENOMEM;
        return -1;
    }
    if (protections_until_failure > 0) --protections_until_failure;
    if (protections_until_foreign_child > 0 && --protections_until_foreign_child == 0) {
        assert(__real_mprotect(foreign_child_page, kExecutablePageSize,
                              PROT_READ | PROT_WRITE | PROT_EXEC) == 0);
        std::memcpy(foreign_child_page + 0xdfc, &foreign_child_value, 4);
        assert(__real_mprotect(foreign_child_page, kExecutablePageSize,
                              PROT_READ | PROT_EXEC) == 0);
    }
    return __real_mprotect(address, size, flags);
}

int main(int argc, char **argv) {
    if (argc == 2 && std::strcmp(argv[1], "config") == 0) {
        AppImage empty{};
        assert(!InstallFolderContract(empty));
        FolderConfig invalid{}; invalid.rows = 9;
        assert(!pengeek_folder_configure(&invalid));
        assert(!g_configured.load());
        FolderConfig defaults{};
        assert(pengeek_folder_configure(&defaults));
        assert(!g_worker_started.load());
        assert(!g_write_started.load());
        assert(!InstallFolderContract(empty));
        assert(!pengeek_folder_configure(&defaults));
        return 0;
    }
    if (argc >= 2 && std::strncmp(argv[1], "live-replay-dynamic-", 20) == 0) {
        FolderConfig c{};
        c.enabled = true;
        c.rows = static_cast<unsigned>(argv[1][20] - '0');
        c.columns = c.rows - 2;
        c.vertical_gap = c.rows;
        c.density_per_mille = 848 + (c.rows - 5) * 50;
        assert(pengeek_folder_configure(&c));
        assert(hc_buffer_rows == c.rows);
        assert(hc_columns_smi == c.columns * 2);
        assert(hc_vertical_gap == c.rows);
        assert(hc_density_ratio == c.density_per_mille / 1000.0);
    } else {
    g_config.enabled = true;
    g_config.rows = 8;
    g_config.columns = 6;
    g_config.hide_new_marker = true;
    g_configured.store(true);
    }

    if (argc == 3 && std::strncmp(argv[1], "live-replay", 11) == 0) {
        const std::string directory = argv[2];
        const int source = open((directory + "/libapp.so").c_str(), O_RDONLY);
        assert(source >= 0);
        const auto size = static_cast<std::size_t>(lseek(source, 0, SEEK_END));
        auto *memory = static_cast<unsigned char *>(mmap(nullptr, size,
            PROT_READ | PROT_WRITE, MAP_PRIVATE, source, 0));
        assert(memory != MAP_FAILED);
        close(source);
        const std::array<std::pair<const char *, std::size_t>, 11> pages{{
            {"height", 0x938000}, {"spacing", 0x93b000}, {"setup", 0xaaa000},
            {"row", 0xaab000}, {"child", 0x93c000}, {"cell", 0xaad000}, {"animation-cell", 0x932000}, {"marker", 0x14a3000}, {"viewport", 0x14c8000}, {"grid-clip", 0x14c7000}, {"drag-window", 0xb85000},
        }};
        for (const auto &[name, offset] : pages) {
            if (std::strcmp(name, "cell") == 0 || std::strcmp(name, "animation-cell") == 0 || std::strcmp(name, "marker") == 0 || std::strcmp(name, "viewport") == 0 || std::strcmp(name, "grid-clip") == 0 || std::strcmp(name, "drag-window") == 0) continue;
            FILE *file = fopen((directory + "/" + name + "-live-page.bin").c_str(), "rb");
            assert(file != nullptr);
            assert(fread(memory + offset, 1, kExecutablePageSize, file) == kExecutablePageSize);
            fclose(file);
        }
        if (std::strcmp(argv[1], "live-replay-drag-overlap") == 0) {
            const std::uint32_t foreign_rows = 0xd28000f0U; // Existing seven-row owner.
            std::memcpy(memory + 0xb85f4c, &foreign_rows, 4);
        }
        const bool veneer = std::strcmp(argv[1], "live-replay-veneer") == 0;
        if (veneer) WriteAbsoluteBranch(memory + 0xaad888,
                                       reinterpret_cast<void *>(0x1234567890ULL));
        const bool satisfied = std::strcmp(argv[1], "live-replay-satisfied") == 0;
        const bool race = std::strcmp(argv[1], "live-replay-child-race") == 0;
        const bool final_check = std::strcmp(argv[1], "live-replay-final-check") == 0;
        const bool failure = final_check || race || std::strcmp(argv[1], "live-replay-failure") == 0;
        if (satisfied) std::memcpy(memory + 0x93cdfc, &kTargetChildInstruction, 4);
        std::array<std::array<unsigned char, kExecutablePageSize>, 11> before{};
        for (std::size_t i = 0; i < pages.size(); ++i)
            std::memcpy(before[i].data(), memory + pages[i].second, kExecutablePageSize);
        const auto &header = *reinterpret_cast<const Elf64_Ehdr *>(memory);
        const auto *sections = reinterpret_cast<const Elf64_Shdr *>(memory + header.e_shoff);
        const char *names = reinterpret_cast<const char *>(memory + sections[header.e_shstrndx].sh_offset);
        AppImage image{};
        image.base = reinterpret_cast<std::uintptr_t>(memory);
        image.size = size;
        std::snprintf(image.path, sizeof(image.path), "%s", (directory + "/libapp.so").c_str());
        image.profile = FindTargetProfile("4f1bdaed7c97ae76de7d3ebc14f6a7b0");
        assert(image.profile != nullptr);
        if (std::strcmp(argv[1], "live-replay-unknown") == 0) {
            image.profile = nullptr;
            image.optimistic = true;
        }
        for (std::size_t i = 0; i < header.e_shnum; ++i) {
            if (std::strcmp(names + sections[i].sh_name, ".text") == 0) {
                assert(sections[i].sh_addr == sections[i].sh_offset);
                image.executable_offset = sections[i].sh_addr;
                image.executable_size = sections[i].sh_size;
            }
        }
        assert(mprotect(memory, size, PROT_READ | PROT_EXEC) == 0);
        if (failure && !final_check) protections_until_failure = 13; // Fail the final density-page write.
        if (race) {
            // Another module writes the target after discovery, before our child write.
            foreign_child_page = memory + 0x93c000;
            protections_until_foreign_child = 4;
            protections_until_failure = 12; // Our child write is now a no-op.
            std::memcpy(before[4].data() + 0xdfc, &kTargetChildInstruction, 4);
        }
        if (final_check) {
            // Restore the child word just as the final density page commits.
            // Installation must fail its post-check and reverse all four pages.
            foreign_child_page = memory + 0x93c000;
            foreign_child_value = ReadInstruction(memory + 0x93cdfc);
            protections_until_foreign_child = 14;
        }
        const bool marker_only = std::strcmp(argv[1], "live-replay-marker") == 0;
        const bool layout_only = std::strcmp(argv[1], "live-replay-layout") == 0;
        const bool marker_failure = std::strcmp(argv[1], "live-replay-marker-failure") == 0;
        if (marker_only || marker_failure) { g_config.enabled = false; }
        if (layout_only) g_config.hide_new_marker = false;
        if (marker_failure) fail_next_protection = true;
        if (marker_failure) {
            assert(!InstallFolderContract(image));
            assert(g_write_started.load());
            assert(!g_contract_committed.load());
            assert(!g_rollback_incomplete.load());
            for (std::size_t i=0; i<pages.size(); ++i)
                assert(!std::memcmp(before[i].data(), memory+pages[i].second, kExecutablePageSize));
            return 0;
        }
        assert(InstallFolderContract(image) != failure);
        if (std::strncmp(argv[1], "live-replay-dynamic-", 20) == 0) {
            const auto mov0 = 0xd2800000U | (g_config.rows << 5);
            const auto mov16 = 0xd2800010U | (g_config.rows << 5);
            for (auto offset : {0x93862c, 0x938634, 0x938644})
                assert(ReadInstruction(memory + offset) == mov0);
            assert(ReadInstruction(memory + 0x93cdfc) == mov16);
            assert(ReadInstruction(memory + 0xb85f4c) == mov16);
            return 0;
        }
        if (marker_only) assert(g_write_started.load());
        if (marker_only || layout_only) {
            for (std::size_t i=0; i<pages.size(); ++i) {
                if ((marker_only && pages[i].second != 0x14a3000) ||
                    (layout_only && pages[i].second == 0x14a3000))
                    assert(!std::memcmp(before[i].data(), memory+pages[i].second, kExecutablePageSize));
            }
            return 0;
        }
        assert(g_contract_committed.load() != failure);
        assert(!g_rollback_incomplete.load());
        if (satisfied || race) assert(!g_child_patch_owned.load());
        const std::array<std::pair<std::size_t, std::size_t>, 16> owned{{
            {0x93862c, 4}, {0x938634, 4}, {0x938644, 4}, {0x93b3e4, 16},
            {0xaaa110, 16}, {0xaabec4, 16}, {0x93cdfc, 4},
            {0x938eac, 16}, {0xaad8ec, 16}, {0x9328f8, 16}, {0xaaa508, 16}, {0x14a3d28, 4}, {0x14c8074, 16}, {0x14c8090, 16}, {0x14c7744, 8}, {0xb85f4c, 4},
        }};
        for (std::size_t i = 0; i < pages.size(); ++i) {
            for (std::size_t byte = 0; byte < kExecutablePageSize; ++byte) {
                const auto offset = pages[i].second + byte;
                bool ours = false;
                for (const auto &[start, length] : owned)
                    ours = ours || (offset >= start && offset < start + length);
                if (failure || !ours) assert(memory[offset] == before[i][byte]);
            }
        }
        if (veneer) {
            std::uintptr_t chained = 0;
            std::memcpy(&chained, memory + 0xaad888 + 8, 8);
            assert(chained == 0x1234567890ULL);
        }
        if (!failure) {
            assert(ReadInstruction(memory + 0xb85f4c) == 0xd2800110U);
            assert(ReadInstruction(memory + 0x14c7744) == 0x91407f70U);
            assert(ReadInstruction(memory + 0x14c7748) == 0xf9408210U);
            assert(ReadInstruction(memory + 0xaa0fa4) == 0xd27c0001U);
            assert(ReadInstruction(memory + 0x14a3d28) == 0x14000004U);
            for (const auto offset : {0x93862c, 0x938634, 0x938644})
                assert(ReadInstruction(memory + offset) == 0xd2800100U);
        }
        munmap(memory, size);
        return 0;
    }
    assert(argc == 2);
    if (std::strcmp(argv[1], "density-text-boundary") == 0) {
        auto *memory = static_cast<unsigned char *>(mmap(nullptr, 8192,
            PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0));
        assert(memory != MAP_FAILED);
        const std::uint32_t calibration[]{0xa9bf79fd, 0xaa0f03fd, 0xd10081ef,
            0xf81f83a1, 0xd2800081, 0x94000000, 0xf85f83a1, 0xf81f03a0, 0xb800f001};
        const std::uint32_t getter[]{0xaa0003e1, 0x94000000, 0xb8473001,
            0x8b1c8021, 0xfc407020, 0xaa1d03ef, 0xa8c179fd, 0xd65f03c0};
        std::memcpy(memory + 4096 - sizeof(calibration), calibration, sizeof(calibration));
        std::memcpy(memory + 64, getter, sizeof(getter));
        assert(mprotect(memory + 4096, 4096, PROT_NONE) == 0);
        AppImage image{};
        image.base = reinterpret_cast<std::uintptr_t>(memory);
        image.size = image.executable_size = 4096;
        ResolvedSymbol calibrate{}, cell{};
        assert(!ResolveDensitySymbols(image, &calibrate, &cell));
        munmap(memory, 8192);
        return 0;
    }
    if (std::strcmp(argv[1], "adrp-veneer") == 0) {
        alignas(16) std::uint32_t input[4]{0x90000011, 0x91258231, 0xd61f0220, 0xd503201f};
        alignas(16) unsigned char output[32]{};
        assert(BuildOriginalEntry(input, output));
        assert(ReadInstruction(output) == 0x58000051U);
        assert(ReadInstruction(output + 4) == 0xd61f0220U);
        std::uintptr_t target = 0;
        std::memcpy(&target, output + 8, 8);
        assert(target == (reinterpret_cast<std::uintptr_t>(input) & ~std::uintptr_t{4095}) + 0x960);
        // Negative ADRP displacement must sign-extend before applying page scale.
        input[0] = 0xf0fffff1U;
        assert(BuildOriginalEntry(input, output));
        std::memcpy(&target, output + 8, 8);
        assert(target == (reinterpret_cast<std::uintptr_t>(input) & ~std::uintptr_t{4095}) - 4096 + 0x960);
        return 0;
    }
    char path[] = "/tmp/hc-page-fixture-XXXXXX";
    const int fd = mkstemp(path);
    assert(fd >= 0);
    std::array<unsigned char, kExecutablePageSize> stock{};
    stock.fill(0x11);
    assert(write(fd, stock.data(), stock.size()) == static_cast<ssize_t>(stock.size()));
    auto *page = static_cast<unsigned char *>(mmap(nullptr, stock.size(),
        PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0));
    assert(page != MAP_FAILED);
    const bool existing = std::strcmp(argv[1], "existing") == 0;
    const bool anonymous = std::strcmp(argv[1], "anonymous") == 0;
    if (existing) std::memset(page + 64, 0x22, 4);
    if (anonymous) {
        assert(munmap(page, stock.size()) == 0);
        page = static_cast<unsigned char *>(mmap(nullptr, stock.size(),
            PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0));
        assert(page != MAP_FAILED);
        std::memcpy(page, stock.data(), stock.size());
    }
    assert(mprotect(page, stock.size(), PROT_READ | PROT_EXEC) == 0);
    if (std::strcmp(argv[1], "child-value") == 0) {
        const std::array<std::uint32_t, 2> instructions{0xd28000f0U, 0x9b107c00U};
        ChildPatchSite site{};
        assert(DeriveChildPatchSite({const_cast<std::uint32_t *>(instructions.data()),
                                    sizeof(instructions)}, &site));
        assert(site.instruction == instructions[0]);
        close(fd);
        unlink(path);
        munmap(page, stock.size());
        return 0;
    }
    AppImage image{};
    image.base = reinterpret_cast<std::uintptr_t>(page);
    image.size = stock.size();
    std::snprintf(image.path, sizeof(image.path), "%s", path);
    ExecutablePageBackup backup{};
    const bool captured = CaptureExecutablePage(page + 128, image, &backup);
    close(fd);
    assert(captured);
    if (std::strncmp(argv[1], "transaction", 11) == 0) {
        CompactLayoutPlan layout{};
        layout.spacing_page = backup;
        layout.height_page = backup;
        layout.spacing_after = backup.bytes;
        layout.height_after = backup.bytes;
        std::memset(layout.spacing_after.data() + 128, 0x33, 16);
        std::memset(layout.height_after.data() + 256, 0x66, 4);
        std::array<HookSpec, 2> hooks{};
        ChildPatchSite child{};
        {
            FolderInstallTransaction transaction{layout, hooks, child};
            assert(CommitLayoutPage(&layout.spacing_page, layout.spacing_after));
            const bool inject_failure = std::strcmp(argv[1], "transaction-failure") == 0;
            fail_next_protection = inject_failure;
            assert(CommitLayoutPage(&layout.height_page, layout.height_after) != inject_failure);
            assert(page[128] == 0x33);
            assert(mprotect(page, stock.size(), PROT_READ | PROT_WRITE | PROT_EXEC) == 0);
            std::memset(page + 512, 0x44, 4);
            assert(mprotect(page, stock.size(), PROT_READ | PROT_EXEC) == 0);
            // No commit: simulate an error / failed final verification.
        }
        assert(page[128] == 0x11 && page[256] == 0x11 && page[512] == 0x44);
        assert(!g_rollback_incomplete.load());
        unlink(path);
        munmap(page, stock.size());
        return 0;
    }
    auto after = backup.bytes;
    std::memset(after.data() + 128, 0x33, 16);
    if (std::strcmp(argv[1], "between") == 0) {
        assert(mprotect(page, stock.size(), PROT_READ | PROT_WRITE | PROT_EXEC) == 0);
        std::memset(page + 64, 0x22, 4);
        assert(mprotect(page, stock.size(), PROT_READ | PROT_EXEC) == 0);
    }
    assert(CommitLayoutPage(&backup, after));
    assert(page[128] == 0x33);
    if (std::strcmp(argv[1], "between") == 0) assert(page[64] == 0x22);
    assert(mprotect(page, stock.size(), PROT_READ | PROT_WRITE | PROT_EXEC) == 0);
    std::memset(page + 512, 0x44, 4); // A different hook installed after ours.
    const bool conflict = std::strcmp(argv[1], "conflict") == 0;
    if (conflict) std::memset(page + 128, 0x55, 4);
    assert(mprotect(page, stock.size(), PROT_READ | PROT_EXEC) == 0);
    const bool restored = RestoreExecutablePage(&backup);
    unlink(path);
    assert(restored == !conflict);
    assert(page[512] == 0x44); // Never erase later modifications elsewhere.
    if (existing) assert(page[64] == 0x22);
    if (conflict) assert(page[128] == 0x55);
    else assert(page[128] == 0x11);
    munmap(page, stock.size());
    std::printf("PASS %s\n", argv[1]);
}
