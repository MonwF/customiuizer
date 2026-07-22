# CustoMIUIzer A14

**米客 A14** 是专为 **HyperOS 1 / Android 14** 维护的系统定制模块，使用 libxposed API 101，并以稳定、轻量和可回退为首要目标。

> [!WARNING]
> 仅支持 Android 14（SDK 34）和 `arm64-v8a`。请勿在 Android 15/16 上启用，也不要同时启用官方版或其他同源分支，否则可能产生重复 Hook。

## 项目状态

| 项目 | 当前值 |
|---|---|
| 应用名 | 米客 A14 |
| 开发版本 | r14.1.3（实机复验中） |
| 稳定回退版本 | r14.1.2 |
| 包名 | `name.monwf.customiuizer.r14` |
| 系统 | HyperOS 1 / Android 14 |
| Hook API | libxposed API 101 |
| LSPosed 基线 | [Vector v2.0-3046](https://github.com/JingMatrix/Vector/actions/runs/29805285935)，commit `9350c7c` |
| 发布页 | [tomthenpc/customiuizer-a14 Releases](https://github.com/tomthenpc/customiuizer-a14/releases) |

r14.1.3 修复候选尚未替换 GitHub 上已有的预发布 APK。完成目标设备复验后，才会更新 Release。

## 主要功能

- 状态栏图标、时钟、电池信息和手势
- 通知展开、小窗、频道与音量控制
- 控制中心布局、运营商显示和主题颜色
- 锁屏、自动亮度、蓝牙与 Wi-Fi 信任
- 最近任务、桌面、导航手势与扩展电源菜单
- 系统应用安装、网络限制和其他 HyperOS 调整

具体功能能否生效取决于系统应用版本和 ROM 改动。未在目标设备验证的功能不承诺兼容。

## 安装

1. 在当前版本中备份设置。
2. 卸载官方版或其他同源分支；不要保留多个模块同时启用。
3. 安装 APK，在 LSPosed 中启用模块并检查作用域。
4. 打开应用一次，然后完整重启设备。
5. 验证设置应用、SystemUI、桌面、锁屏和常用 Hook 功能。

同包名、同签名且版本号不低于已安装版本时可以覆盖安装；否则请先备份再卸载。

## r14.1.3 重点

- 修复 R8 重命名 `after` 回调后，Launcher 与 SystemUI 后置 Hook 被整体跳过的问题。
- 保留 Hook 回调与 AndroidX 启动链隔离，避免模块生效但设置应用无法打开。
- 移除版本下载、代码仓库、赞赏入口、内置网页及不再需要的网络权限。
- 将应用图标加载收敛到单个有界线程池，并限制图标缓存上限。
- 减少音频可视化静音帧扫描和主线程位图比较。
- 修复 Android 14 动态广播注册与 Wi-Fi 权限拒绝边界。

完整变更、历史版本和静态性能评估见 [CHANGELOG.md](CHANGELOG.md)。

## 构建

要求 JDK 17 与 Android SDK：

```powershell
.\gradlew.bat clean assembleRelease lintRelease lintVitalRelease
```

Release 配置：

- compileSdk 36，minSdk/targetSdk 34
- R8、资源压缩和 zipalign
- APK Signature Scheme v2
- 输出：`app/build/outputs/apk/release/CustoMIUIzer-A14-r14.1.3.apk`

本地正式签名从仓库外部的 `../keystore.properties` 读取；密钥和口令不得提交。

## 项目关系与许可

CustoMIUIzer A14 是独立维护的下游衍生项目，不代表原作者或上游项目。代码源自 [Mikanoshi/CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer)，Android 14 基线参考 [MonwF/customiuizer](https://github.com/MonwF/customiuizer) 的 `v24.10.12` 版本。

项目继续以 [GPL-3.0](LICENSE) 发布。分发 APK 时必须同时提供对应源码、保留许可证与版权来源，并明确标注修改。详见 [NOTICE.md](NOTICE.md)。

[English](README_en.md) | [日本語](README_jp.md) | [Português (Brasil)](README_PT-BR.md) | **简体中文**
