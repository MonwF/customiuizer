# CustoMIUIzer A14

**米客 A14** is an independently maintained system customization module for **HyperOS 1 / Android 14**. It uses [MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) as its A14 functional reference while maintaining its own package, release line, build, and validation process.

Its two defining differences are **libxposed API 101 integration** and ongoing **code/resource optimization**. Hook hot paths, executors, caches, reflection, and error boundaries are refined without sacrificing reliable post-reboot injection.

> [!WARNING]
> Android 14 (SDK 34) and `arm64-v8a` only. Do not enable it on Android 15/16 or alongside another CustoMIUIzer-derived module.

## Status

| Item | Value |
|---|---|
| App name | 米客 A14 |
| Stable release | r14.2.0 |
| Previous stable release | r14.1.3 |
| Package | `name.monwf.customiuizer.r14` |
| Hook API | libxposed API 101 |
| LSPosed baseline | [Vector v2.0-3046](https://github.com/JingMatrix/Vector/actions/runs/29805285935), commit `9350c7c` |
| Releases | [tomthenpc/customiuizer-a14](https://github.com/tomthenpc/customiuizer-a14/releases) |

The target device passed installation, a full reboot, and the relevant Launcher/SystemUI checks. The final r14.2.0 logs contain no module-owned exception and no app, SystemUI, or launcher crash, ANR, or process death.

## Highlights

- Status bar, notifications, Control Center, lock screen, launcher, recents, navigation, and power-menu customizations.
- R8-safe `after` callback detection for Launcher and SystemUI hooks.
- Separation between Xposed callback types and the normal Android app startup path.
- Removed download, repository, donation, embedded web content, and app network permission.
- Bounded shared icon executor and cache, plus reduced audio visualizer work.

Feature compatibility depends on the exact Xiaomi system-app and ROM versions. See [CHANGELOG.md](CHANGELOG.md) for release history and measured/static comparisons.

## Install

1. Back up settings from the currently installed build.
2. Remove other upstream or derivative builds; never enable two copies together.
3. Install the APK, enable it in LSPosed, and verify the scope.
4. Open the app once, then perform a full reboot.
5. Test the settings app, SystemUI, launcher, lock screen, and your regular hooks.

## Build

JDK 17 and the Android SDK are required:

```powershell
.\gradlew.bat clean assembleRelease lintRelease lintVitalRelease
```

Release builds use R8, resource shrinking, zipalign, and APK Signature Scheme v2. Signing configuration is read from `../keystore.properties`; never commit keys or passwords.

## Origin and license

CustoMIUIzer A14 is an independent downstream derivative and is not affiliated with or endorsed by its upstream authors. It derives from [Mikanoshi/CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer) and the Android 14 work in [MonwF/customiuizer](https://github.com/MonwF/customiuizer), with `v24.10.12` used as the functional A14 reference.

Distributed under [GPL-3.0](LICENSE). Binary distributions must provide the corresponding source, retain license and copyright notices, and identify modifications. See [NOTICE.md](NOTICE.md).

**English** | [日本語](README_jp.md) | [Português (Brasil)](README_PT-BR.md) | [简体中文](README.md)
