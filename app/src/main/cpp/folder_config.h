#pragma once
#include <cstdint>
// Immutable process-lifetime snapshot; configure before any native load callback.
struct FolderConfig {
    bool enabled = false;
    std::uint32_t columns = 3;
    std::uint32_t rows = 5;
    std::uint32_t vertical_gap = 0;
    std::uint32_t density_per_mille = 1000;
    bool hide_new_marker = false;
};
inline bool ValidFolderConfig(const FolderConfig &c) {
    return c.columns >= 3 && c.columns <= 6 && c.rows >= 5 && c.rows <= 8 &&
        c.vertical_gap <= 20 && c.density_per_mille >= 848 && c.density_per_mille <= 1000;
}
extern "C" bool pengeek_folder_configure(const FolderConfig *config);

void StartFolderConfigTransport();
