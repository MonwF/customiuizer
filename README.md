# 米客_forA14

基于 [MonwF/customiuizer](https://github.com/MonwF/customiuizer) 的非官方轻量化 fork，面向 **HyperOS 1 / Android 14 / libxposed API 101**。

> [!IMPORTANT]
> 安装前请在应用内备份设置，并卸载官方版或其他同类 fork。不要同时启用多个版本，以免重复 Hook。

## 当前版本

- 版本：**r14.1.3**（versionCode 117）
- 包名：`name.monwf.customiuizer.r14`
- 架构：`arm64-v8a`
- 分支：`a14-api101`
- 发布页：[GitHub Releases](https://github.com/tomthenpc/customiuizer/releases/tag/r14.1.3)

r14.1.3 从已验证可用的 Devin r14.1.2 基线重新构建，保持原有 Hook 架构：`GlobalActions`、`Controls`、`Launcher`、`System`、`Various` 使用 API 101 原生 `intercept(Chain)`，`SystemUI` 保留兼容适配层。

本版重点：

- 移除“版本下载、代码仓库、微信/PayPal 赞赏”等支持入口及内置网页、图片和网络权限。
- 隔离普通应用启动路径与 compileOnly libxposed Hook 回调，避免设置应用启动时解析 Xposed API。
- 将 4 个应用列表的独立图标线程池合并为 1 个有界共享池，限制并发和积压任务。
- 将图标缓存由“最多半个 Java 堆”限制为 1–16 MiB，并移除主动 GC。
- 音频可视化每帧只进行一次静音 FFT 判断，并避免主线程逐像素比较专辑图。
- 修复 Wi‑Fi 权限拒绝、Android 14 动态广播注册等运行时边界问题。
- 完整 `lintRelease` 从基线 27 个错误降为 0。

详细变更和与 r14.1.2 的评估见 [CHANGELOG.md](CHANGELOG.md)。

## 安装与验证

1. 在旧版本中备份设置。
2. 卸载官方版或其他 fork，再安装本版 APK。
3. 在 LSPosed 中启用模块并确认作用域。
4. 重启设备后检查模块状态、SystemUI、桌面和常用功能。

本项目只面向 Android 14。请勿在 Android 15/16 上启用。

## 主要功能

- 状态栏布局、图标、显秒、电池温度与彩色电池条
- 通知展开、小窗、频道与通知音量控制
- 锁屏、手电筒、蓝牙/Wi‑Fi 信任与自动亮度设置
- 桌面、导航手势、浮窗与扩展电源菜单
- 系统应用安装、网络限制及其他 HyperOS 调整

## 构建信息

- compileSdk 36；minSdk/targetSdk 34
- libxposed API 101
- Release 启用 R8、资源压缩和 zipalign
- APK 使用 v2 签名

## 项目说明

- 上游：[MonwF/customiuizer](https://github.com/MonwF/customiuizer)
- 原始项目：[Mikanoshi/CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer)
- 协议：[GPL-3.0](LICENSE)

[English](README_en.md) | [日本語](README_jp.md) | [Português (Brasil)](README_PT-BR.md) | **中文**
