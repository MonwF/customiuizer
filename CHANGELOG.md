# Changelog

> This is an **unofficial optimized fork** of [MonwF/customiuizer](https://github.com/MonwF/customiuizer), released under the same **GPL-3.0** license.  
> The original module is by `MonwF`; the upstream source is by `Mikanoshi` (CustoMIUIzer).
> Primary target: **HyperOS 1 / Android 14 (API 34) / libxposed API 101**.

## Base

- Forked from `MonwF/customiuizer` `a14` branch (`open source for a14`).
- Migrated lifecycle and hook registration to **libxposed API 101**.
- Preserved API-100-style callback behavior (mutable arguments, early return/throw, result replacement and throwable recovery).
- Restricted initialization to Android 14 (`UPSIDE_DOWN_CAKE`) to avoid applying HyperOS 1 hooks to incompatible Android 15/16 components.

## r3 — class cache

- Added class-level cache in `XposedHelpers` to avoid repeated `Class.forName` lookups.

## r4 — context cache + resource map lookup reduction

- Cached `ModuleHelper.findContext()` result.
- Reduced duplicate map lookups in `ResourceHooks.getResourceReplacement()`.

## r5 — resource handle reuse

- Cached the module `Resources` object once per replacement call and passed it down to `getFakeResource` / `getResourceReplacement`.

## r6 — theme value pre-parsing

- Extended `ResourceHooks.ThemeValue` with pre-parsed fields (`pkg`, `name`, `themeValueType`, `resourceType`) so `setThemeValueReplacement` and `initThemeHook` no longer re-split strings.

## r7 — direct resource value dispatch

- Replaced reflection-based resource value retrieval with a direct `switch` dispatch in `ResourceHooks.getModuleResValue()`.

## r8 — direct user id + Dependency instance cache

- Replaced `UserHandle.getUserId(Process.myUid())` reflection with direct arithmetic (`Process.myUid() / 100000`).
- Added `ConcurrentHashMap` instance cache and cached `Dependency.get` `Method` in `ModuleHelper.getDepInstance()`.

## r9 — zero-argument call overloads

- Added zero-argument overloads for `XposedHelpers.callMethod`, `callStaticMethod`, `newInstance`, `findMethodBestMatch` and `findConstructorBestMatch`.
- Shared `EMPTY_OBJECT_ARRAY` / `EMPTY_CLASS_ARRAY` singletons and reused an empty `Object[]` in `HookerClassHelper.BeforeHookCallback` for argument-less hooks.

## r10 — constant hook fast path

- Added `ConstantHooker` for `DO_NOTHING` / `returnConstant` callbacks; these bypass `BeforeHookCallback`/`AfterHookCallback` creation and return the value directly.

## r11 — parameter class cache

- Cached resolved `Class<?>[]` results of `XposedHelpers.getParameterClasses()` by `(ClassLoader, parameter signature)` key to reduce repeated `findClass` work during hook registration.

## r12 — module resource configuration cache

- Cached the module `Resources` object in `ModuleHelper.getModuleRes()` and reused it while the device `Configuration` is unchanged; recreated on configuration changes.

## r13 — skip empty after-callbacks

- `CustomHooker` now detects whether a `MethodHook` overrides `after()`; if not, it skips `AfterHookCallback` creation and invocation.

## r14 — resource hook early-exit

- `ResourceHooks.mReplaceHook.before` now checks `fakes` / `resourceIdReplacements` before fetching the module context or resources.
- `OBJECT`-type replacements return immediately without touching module resources.
- Reused the `Integer` object already present in `param.getArgs()[0]` as the `ConcurrentHashMap` key to avoid extra boxing.

## Build / verification

- APK: `Pengeek-HyperOS1-A14-API101-r14.apk`
- `versionCode`: `107`
- `versionName`: `24.10.12-hos1-a14-api101-r14`
- Signed with debug keystore for local/test use.
- Verified with `zipalign` and `apksigner` (v3 signature).

## Tested devices

| Device | HyperOS | Android | SoC | RAM | Notes |
|--------|---------|---------|-----|-----|-------|
| Xiaomi 13 (2211133G) | 1.0.7.0.UMCTWXM | 14 (UKQ1.230804.001) | Snapdragon 8 Gen 2 (up to 3.19 GHz) | 12 GB | Primary test device; r3–r14 rebooted and loaded normally. |

- Baseband: `MPSS.DE.3.0.c1-GLB-Oct 17 2024-04:43:46`
- Kernel: `5.15.123-android13-8-00008-g3ca6a2912c7e-ab11087001`
