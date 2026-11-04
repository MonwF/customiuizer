#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <cstring>

struct TargetProfile {
    const char *version_name;
    const char *build_id;
    std::uintptr_t setup_view_offset;
    std::size_t setup_view_size;
    std::uintptr_t row_num_offset;
    std::size_t row_num_size;
    std::uintptr_t get_child_count_offset;
    std::size_t get_child_count_size;
};

constexpr std::array<TargetProfile, 3> kTargetProfiles{{
    {
        "801027654 / RELEASE-8.01.02.7654",
        "4f1bdaed4617656dde7d3ebcd2566b75",
        0xa85478,
        0x324,
        0xa871b0,
        0x30,
        0x907e48,
        0xb4,
    },
    {
        "801026179 / RELEASE-8.01.02.6179",
        "4f1bdaedba4ed60ade7d3ebc0fbbdc89",
        0xa8b704,
        0x430,
        0xa8d548,
        0x30,
        0x8d909c,
        0xb4,
    },
    {
        "801028593 / RELEASE-8.01.02.8593",
        "4f1bdaed7c97ae76de7d3ebc14f6a7b0",
        0xaaa110,
        0x324,
        0xaabec4,
        0x30,
        0x93cdbc,
        0xb4,
    },
}};

inline const TargetProfile *FindTargetProfile(const char *build_id) {
    if (build_id == nullptr || build_id[0] == '\0') return nullptr;
    for (const TargetProfile &profile : kTargetProfiles) {
        if (std::strcmp(profile.build_id, build_id) == 0) return &profile;
    }
    return nullptr;
}
