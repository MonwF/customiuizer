# HyperOS 1 / Android 14 / libxposed API 101 build

This branch is based on MonwF/customiuizer `a14` at commit
`6ce3a146c424e353d5a477ece3cee27ff19eb385` (`open source for a14`).

Target environment:

- Xiaomi HyperOS 1 based on Android 14 (SDK 34)
- Xiaomi 13 / model 2211133G is the primary target
- arm64-v8a
- Vector/libxposed API 101

Compatibility and stability changes:

- Migrated module lifecycle and hook registration to libxposed API 101.
- Preserved API 100 callback behavior for mutable arguments, early return/throw,
  result replacement and throwable recovery.
- Kept constant-return hooks at highest priority without the API 101 integer overflow.
- Disabled hook initialization outside Android 14 to avoid applying HyperOS 1 hooks
  to incompatible Android 15/16 system components.
- Limited the Security Center sidebar receiver hook to its UI process.
- Removed an obsolete preference path that incorrectly installed the app-sort hook.
- Included the Traditional Chinese (`zh-rTW`) resources used by the Taiwan ROM.
- Updated dependencies to the versions used by the upstream API 101 build where they
  are independent of HyperOS 2 implementation details.

The package name remains `name.monwf.customiuizer`; this is an upgrade/test build, not
a side-by-side application. The included release artifact is locally debug-signed.
