/*
 * Minimal Dart AOT contract for the validated HyperOS 4 folder layout.
 * Dart keeps its managed stack in x15. Exit hooks replay the original
 * epilogue; no new managed frame or C/C++ call is introduced.
 */

    .text
    .p2align 2

    .macro LOAD_GLOBAL_W reg, symbol
    adrp x\reg, \symbol
    ldr  w\reg, [x\reg, :lo12:\symbol]
    .endm

    .macro LOAD_GLOBAL_X reg, symbol
    adrp \reg, \symbol
    ldr  \reg, [\reg, :lo12:\symbol]
    .endm

    .macro STORE_DIAG_X symbol, reg
    adrp x14, :got:\symbol
    ldr  x14, [x14, :got_lo12:\symbol]
    str  \reg, [x14]
    .endm

    .macro INCREMENT_DIAG symbol
    adrp x13, :got:\symbol
    ldr  x13, [x13, :got_lo12:\symbol]
.Ldiag_retry\@:
    ldxr x14, [x13]
    add  x14, x14, #1
    stxr w16, x14, [x13]
    cbnz w16, .Ldiag_retry\@
    .endm

    .global hc_row_num_hook
    .hidden hc_row_num_hook
    .type hc_row_num_hook, %function
hc_row_num_hook:
    LOAD_GLOBAL_W 9, hc_contract_active
    cbz  w9, 1f
    INCREMENT_DIAG folder_diag_row_num_calls
    LOAD_GLOBAL_W 0, hc_buffer_rows
    STORE_DIAG_X folder_diag_row_num_last, x0
    ret
1:
    LOAD_GLOBAL_X x17, hc_row_num_original
    cbz  x17, 2f
    br   x17
2:
    mov  x0, #0
    ret
    .size hc_row_num_hook, .-hc_row_num_hook

    .global hc_setup_view_hook
    .hidden hc_setup_view_hook
    .type hc_setup_view_hook, %function
hc_setup_view_hook:
    LOAD_GLOBAL_W 9, hc_pool_base_offset
    cbz  w9, 4f
    ldr  x10, [x26, x9]
    cbz  x10, 4f
    LOAD_GLOBAL_W 11, hc_columns_slot_offset
    add  x10, x10, x11
    ldr  x12, [x10]
    STORE_DIAG_X folder_diag_pool_value_last, x12

    LOAD_GLOBAL_W 9, hc_contract_active
    // Before commit or after failure, pass through without claiming ownership
    // of a six-column value which may have been written by another module.
    cbz  w9, 4f
    cmp  x12, #6                 // stock Smi(3)
    b.eq 1f
    LOAD_GLOBAL_W 11, hc_columns_smi
    cmp  x12, x11
    b.eq 3f
    STORE_DIAG_X folder_diag_pool_invalid_last, x12
    b    4f
1:
    LOAD_GLOBAL_W 12, hc_columns_smi
    str  x12, [x10]
    b    3f
3:
    INCREMENT_DIAG folder_diag_setup_calls
4:
    LOAD_GLOBAL_X x17, hc_setup_view_original
    cbz  x17, 5f
    br   x17
5:
    ret
    .size hc_setup_view_hook, .-hc_setup_view_hook

    .hidden hc_contract_active
    .hidden hc_buffer_rows
    .hidden hc_pool_base_offset
    .hidden hc_columns_slot_offset
    .hidden hc_row_num_original
    .hidden hc_setup_view_original

/*
 * Candidate spacing exit for the verified 801028593 calItemSpacing frame.
 * Not installed yet: runtime installer must verify the complete producer,
 * saved receiver slot, vertical stores, and shared epilogue before using it.
 * Preserve horizontal spacing; remove additional vertical spacing.
 * Cell height and icon dimensions are independent of this extra gap.
 * No allocation, calls, managed references on a new stack, or GC safepoints.
 */
    .global hc_compact_spacing_exit
    .hidden hc_compact_spacing_exit
    .type hc_compact_spacing_exit, %function
hc_compact_spacing_exit:
    LOAD_GLOBAL_W 16, hc_contract_active
    cbz w16, .Lspacing_passthrough
    ldur x16, [x29, #-8]
    adrp x17, hc_vertical_gap
    ldr d0, [x17, :lo12:hc_vertical_gap]
    stur d0, [x16, #0x3b]
.Lspacing_passthrough:
    // Replay the original shared calItemSpacing epilogue exactly.
    mov x0, x22
    mov x15, x29
    ldp x29, x30, [x15], #0x10
    ret
    .size hc_compact_spacing_exit, .-hc_compact_spacing_exit

    .section .note.GNU-stack,"",%progbits

    .text
    .macro SCALE_DENSITY
    LOAD_GLOBAL_W 16, hc_contract_active
    cbz w16, 1f
    str q1, [sp, #-16]!
    adrp x16, hc_density_ratio
    ldr d1, [x16, :lo12:hc_density_ratio]
    fmul d0, d0, d1
    ldr q1, [sp], #16
1:
    .endm
    .global hc_density_exit
    .hidden hc_density_exit
    .hidden hc_density_ratio
    .type hc_density_exit, %function
hc_density_exit:
    // All three original tails load the boxed result through x1 before returning.
    // No call, allocation, changed return PC or managed frame is introduced.
    ldur d0, [x1, #7]
    SCALE_DENSITY
    mov x15, x29
    ldp x29, x30, [x15], #16
    ret
    .size hc_density_exit, .-hc_density_exit

    .global hc_density_cache
    .hidden hc_density_cache
    .hidden hc_density_cache_resume
    .type hc_density_cache, %function
hc_density_cache:
    ldur d0, [x1, #7]
    SCALE_DENSITY
    // Replay the allocation guard before resuming at its original B.LS.
    // The slow path and Dart safepoint remain in the original function.
    ldp x0, x1, [x26, #0x60]
    add x0, x0, #0x10
    cmp x1, x0
    LOAD_GLOBAL_X x16, hc_density_cache_resume
    br x16
    .size hc_density_cache, .-hc_density_cache


.hidden hc_vertical_gap
.hidden hc_columns_smi
