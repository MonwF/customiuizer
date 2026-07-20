## Pengeek(r14)

> [!NOTE]
> This is an **unofficial optimized fork** based on [MonwF/customiuizer](https://github.com/MonwF/customiuizer) under the **GPL-3.0** license.
> It is intended for personal study and performance optimization. Original credit goes to **MonwF** and **Mikanoshi**.
> Compatible only with: **HyperOS 1 / Android 14 / libxposed API 101**.
> This fork uses the native **libxposed API 101 `XposedInterface.Hooker.intercept(Chain)`** dispatch, so it should work with frameworks that support API 101 such as **LSPosed 2.0** and **Vector**. So far it has only been tested on LSPosed + HyperOS 1 A14.
> Package changed to `name.monwf.customiuizer.r14`, app name is **Pengeek(r14) / 米客(r14)**。
> **Before installing this fork, please back up your settings inside the 米客/Pengeek app first, then uninstall the official MonwF build before installing this fork, and enable only this module. Two Xposed modules hooking the same system cannot be enabled at the same time.**
> Version plan: **r14.0.\*** keeps the API-100 style callback adapter; **r14.1.\*** will be a native API-101 rewrite.
> See [CHANGELOG.md](CHANGELOG.md) for the full optimization history and release notes.

**English** | [日本語](README_jp.md) | [Português (Brasil)](README_PT-BR.md) | [中文](README.md)

Customize your HyperOS to your liking.

For `HyperOS` based on `Android 14`.

> Thanks for the awesome module [CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer) from `Mikanoshi`

### Legacy releases

* [MIUI 14 release](https://github.com/MonwF/customiuizer/releases/tag/v23.11.26)
* [MIUI 13 release](https://github.com/MonwF/customiuizer/releases/tag/v23.08.26)

## Translations
[![Crowdin](https://badges.crowdin.net/customiuizer14/localized.svg)](https://crowdin.com/project/customiuizer14)

## Upstream & release

* Original repository: [MonwF/customiuizer](https://github.com/MonwF/customiuizer)
* This fork release: see [CHANGELOG.md](CHANGELOG.md)
* License: GPL-3.0 (same as upstream)

## Tested devices

| Device | HyperOS | Android | SoC | RAM | Notes |
|--------|---------|---------|-----|-----|-------|
| Xiaomi 13 (2211133G) | 1.0.7.0.UMCTWXM | 14 (UKQ1.230804.001) | Snapdragon 8 Gen 2 (up to 3.19 GHz) | 12 GB | Primary test device; r3–r14 rebooted and loaded normally. |

* Baseband: `MPSS.DE.3.0.c1-GLB-Oct 17 2024-04:43:46`
* Kernel: `5.15.123-android13-8-00008-g3ca6a2912c7e-ab11087001`

## Here are the main working features:
* Keep phone unlocked in a trusted environment (Bluetooth and Wi-Fi)
* Autobrightness range limit
* Extended timers for silent and dnd mode
* Music visualizer
* Set album art as wallpaper
* Statusbar
  * Hide icons
  * Clock tweak
  * Gesture (double tap to sleep, slice to adjust volume or brightness)
  * Battery bar indicator
* Notifications
  * Notification importance
  * Auto expand
  * Extended menu
  * Remove limit per package
  * Open in floating window
  * Open channel settings
* Disable blacklist for floating windows
* Extended power menu
* Allow downgrade
* Disable app signature verification
* Use back gesture with navbar mode
* Custom left/right buttons on the navbar
* Skip permission intercept warning countdown
* Control system apps to connect with Wi-Fi and battery saver
* Show more detail on app installer or info activity
* Powerful gesture with multi actions
