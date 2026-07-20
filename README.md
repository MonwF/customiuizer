## 米客_forA14

> [!注意]
> 本项目是基于 [MonwF/customiuizer](https://github.com/MonwF/customiuizer) 的**非官方优化 fork**，沿用 **GPL-3.0** 协议，仅供个人学习交流。  
> 仅兼容 **HyperOS 1 / Android 14 / libxposed API 101**，已在 **小米 13 / HyperOS 1.0.7.0.UMCTWXM / Android 14 / LSPosed** 实机测试。  
> 安装前请先在应用内备份设置，卸载官方原版后安装本 fork；请勿与官方版或其他 fork 同时启用，否则会出现重复 hook 冲突。

[English](README_en.md) | [日本語](README_jp.md) | [Português (Brasil)](README_PT-BR.md) | **中文**

### 项目简介

米客_forA14 是 customiuizer 的 HyperOS 1 / Android 14 优化 fork，目标是在 libxposed API 101 下以原生 `XposedInterface.Hooker.intercept(Chain)` 调度所有 hook，降低适配层开销、减少临时对象分配。

### 版本规划

- **r14.0.***：`a14` 分支，API-100 兼容实现。
- **r14.1.***：`a14-api101` 分支，原生 API-101 实现。
  - `r14.1.0`：完成 `GlobalActions` / `Controls` 迁移。
  - `r14.1.3`：完成 `Launcher` / `System` / `SystemUI` / `Various` 迁移，修复重启后 `RemotePreferences` 未同步导致 hook 失效的问题。

完整记录见 [CHANGELOG.md](CHANGELOG.md)。

### 安装说明

1. 在 米客_forA14 应用内备份设置。
2. 卸载官方原版或其他 fork。
3. 安装本 fork APK 并在 LSPosed 中启用。
4. 重启设备。

### 构建与签名

```bash
./gradlew :app:assembleRelease
```

- 构建产物：`app/build/outputs/apk/release/Pengeek-HyperOS1-A14-API101-r14.1.3.apk`
- 使用 release keystore 进行 v2 签名，可通过 `apksigner verify -v` 校验。

### 测试设备

| 设备 | HyperOS | Android | SoC | 内存 | 说明 |
|---|---|---|---|---|---|
| 小米 13 (2211133G) | 1.0.7.0.UMCTWXM | 14 | Snapdragon 8 Gen 2 | 12 GB | 主要测试机；r3–r14.1.3 均正常重启并加载。 |

### 主要功能

- 双排状态栏 / 双排信号 / 状态栏显秒与图标隐藏
- 信任蓝牙和 Wi-Fi 禁止锁屏、查看已保存 Wi-Fi 密码
- 自动亮度范围限制、状态栏电池温度 / 电池显示
- 跳过 10s 安全警告
- 音乐可视化 / 专辑封面壁纸
- 独立通知音量、通知重要性 / 自动展开 / 小窗打开
- 浮窗状态记忆、移除黑名单（含分屏）
- 扩展电源菜单、锁屏手电筒、导航栏手势与自定义按钮
- 允许直接更新系统应用、安装/升级时显示更多信息
- 允许限制系统 app 使用网络

### 上游与协议

- 上游：[MonwF/customiuizer](https://github.com/MonwF/customiuizer)
- 协议：**GPL-3.0**

---

> 本 fork 仅供学习交流。
