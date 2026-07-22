# 米客_forA14

基于 [MonwF/customiuizer](https://github.com/MonwF/customiuizer) 的非官方轻量优化版本，面向 **HyperOS 1 / Android 14 / libxposed API 101**。包名为 `name.monwf.customiuizer.r14`，不能与官方版或其他 fork 同时启用。

当前稳定版：**r14.1.2 修复版**（versionCode 116） · [下载与发布说明](https://github.com/tomthenpc/customiuizer/releases/tag/r14.1.2)

[English](README_en.md) | [日本語](README_jp.md) | [Português (Brasil)](README_PT-BR.md) | **中文**

## 版本特点

- 原生使用 libxposed API 101 的 `XposedInterface.Hooker.intercept(Chain)`。
- `GlobalActions`、`Controls`、`Launcher`、`System`、`Various` 已完成原生 API 101 迁移。
- `SystemUI` 保留经 r14.1.1 实机验证的兼容适配层，避免完整迁移造成重启后 Hook 失效。
- 设置变更同步至 LSPosed Remote Preferences；不增加后台服务、轮询或开机常驻任务。
- 已隔离 AndroidX Startup 与 Xposed 专用类的 R8 优化边界，修复重启后 Hook 生效但设置应用无法打开的问题。
- r14.1.2 收敛图标加载线程与缓存，减少重复反射、临时对象和音频可视化计算。
- 已移除应用内版本下载、代码仓库、赞赏入口及其网页/图片资源。

完整变更和实测 APK 对比见 [CHANGELOG.md](CHANGELOG.md)。

## 安装

1. 在现有模块中备份设置。
2. 卸载官方版或其他同包冲突版本，再安装本 fork。
3. 在支持 libxposed API 101 的框架中启用模块并选择作用域。
4. 完整重启设备后检查常用 Hook；升级前建议保留上一版 APK 以便回退。

## 兼容性

| 项目 | 支持范围 |
|---|---|
| 系统 | HyperOS 1 / Android 14（API 34） |
| Hook API | libxposed API 101 |
| 主要验证环境 | 小米 13（2211133G），HyperOS 1.0.7.0.UMCTWXM |
| 不支持 | Android 15/16、MIUI 旧版本、与其他 customiuizer 分支同时启用 |

模块涉及系统进程 Hook，不同地区 ROM 或系统应用版本可能存在差异。首次安装或修改关键设置后应重启验证。

## 构建

```powershell
./gradlew.bat clean assembleRelease
```

正式发布需在仓库上级目录提供 `keystore.properties`。未配置发布签名时，Gradle 会回退到调试签名，不能用于覆盖安装正式版。

## 上游与协议

- 上游：[MonwF/customiuizer](https://github.com/MonwF/customiuizer)
- 原始项目：[Mikanoshi/CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer)
- 许可证：[GPL-3.0](LICENSE)

本项目仅供学习、适配与性能优化研究；原始作品版权归其作者所有。
