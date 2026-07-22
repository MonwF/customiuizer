# CustoMIUIzer A14

**米客 A14** 是面向 **HyperOS 1 / Android 14** 的独立维护版系统定制模块。项目以 [MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) 为 Android 14 功能参考，但采用自己的包名、版本线、构建与发布流程。

本项目与参考版本的核心区别只有两条，也会作为后续维护主线：

1. **libxposed API 101**：适配 Vector/LSPosed API 101，以原生 `intercept(Chain)` 和经过实机验证的兼容层组合运行。
2. **代码与资源优化**：持续治理 Hook 热路径、线程、缓存、反射、资源和异常边界，减少无效工作与内存峰值，同时优先保证重启后 Hook 可靠。

> [!WARNING]
> 仅支持 Android 14（SDK 34）和 `arm64-v8a`。不要在 Android 15/16 上启用，也不要与上游版或其他同源分支同时启用，否则可能产生重复 Hook。

## 当前版本

| 项目 | 当前值 |
|---|---|
| 当前稳定版 | r14.2.0 |
| 上一稳定版 | r14.1.3 |
| 应用名 | 米客 A14 |
| 包名 | `name.monwf.customiuizer.r14` |
| 目标系统 | HyperOS 1 / Android 14 |
| Hook 接口 | libxposed API 101 |
| LSPosed 基线 | [Vector v2.0-3046](https://github.com/JingMatrix/Vector/actions/runs/29805285935)，commit `9350c7c` |
| 发布页 | [tomthenpc/customiuizer-a14 Releases](https://github.com/tomthenpc/customiuizer-a14/releases) |

r14.2.0 已完成安装、完整重启、常用功能与 LSPosed 日志验证。最终启动周期内没有本模块异常，也没有应用、SystemUI 或桌面的崩溃、ANR 与进程死亡。

## 与参考版本的区别

| 维度 | CustoMIUIzer A14 |
|---|---|
| 维护范围 | 固定面向 HyperOS 1 / Android 14，避免向未验证系统注册 Hook |
| Hook 架构 | libxposed API 101；按模块使用原生拦截器或稳定兼容层 |
| 安装身份 | 独立 applicationId，可与参考源码和发布历史区分 |
| 性能策略 | 优化高频 Hook、共享有界线程池、限制缓存、减少反射和重复计算 |
| 轻量化 | 移除下载、仓库、赞赏、内置网页与网络权限 |
| 发布策略 | 每版执行混淆构建、Lint、zipalign、签名和实机重启验证 |

“独立维护”不表示抹去原作者关系：本项目仍是 GPL-3.0 衍生作品，保留来源、许可证与对应源码义务。

## 功能范围

- 状态栏图标、时钟、电池信息和手势
- 通知展开、小窗、频道与音量控制
- 控制中心布局、运营商显示和主题颜色
- 锁屏、自动亮度、蓝牙与 Wi-Fi 信任
- 最近任务、桌面、导航手势与扩展电源菜单
- 系统应用安装、网络限制和其他 HyperOS 调整

功能兼容性取决于 Xiaomi 系统应用和 ROM 的具体版本。未在目标设备验证的组合不作兼容承诺。

## 安装

1. 在当前版本中备份设置。
2. 卸载上游版或其他同源分支，不要保留多个模块同时启用。
3. 安装 APK，在 LSPosed 中启用模块并检查作用域。
4. 打开应用一次，然后完整重启设备。
5. 验证设置应用、SystemUI、桌面、锁屏和常用 Hook。

只有包名、签名一致且新 APK 的版本号不低于已安装版本时才能覆盖安装；其他情况请先备份再卸载。

## r14.2.0 摘要

- 偏好键在加载时一次规范化，高频 Hook 读取不再反复拼接 `pref_key_` 或重复查表。
- API 101 兼容层按需生成参数数组；清除 164 处无效/无改写参数复制和 116 次无效对象读取。
- Hook 注册不再为参数签名构造并长期保留重复字符串缓存，兼容层的后置回调检测延迟到首次实际执行。
- 秒级时钟刷新改为主线程单调度，不再额外创建 `Timer` 后台线程和跨线程转发。
- 修复音量面板模糊参数的动态更新键不匹配，并加强偏好观察者的并发安全。
- 保持 r14.1.3 已验证的 API 101 混合架构、SystemUI 空值保护、独立包名与无网络权限边界。

完整版本记录和相对 r14.1.3 的静态性能评估见 [CHANGELOG.md](CHANGELOG.md)。

## 构建

需要 JDK 17 与 Android SDK：

```powershell
.\gradlew.bat --no-daemon clean assembleRelease lintRelease lintVitalRelease
```

Release 构建启用 R8、资源压缩、zipalign 和 APK Signature Scheme v2。正式签名从仓库外的 `../keystore.properties` 读取，密钥与口令不得提交。

## 来源与许可

CustoMIUIzer A14 由 `tomthenpc` 独立维护，不代表原作者或参考项目。源码来自 [Mikanoshi/CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer)，Android 14 功能基线参考 [MonwF/customiuizer](https://github.com/MonwF/customiuizer) 的 `v24.10.12`。

项目继续以 [GPL-3.0](LICENSE) 发布。分发 APK 时必须提供对应源码、保留许可证与版权来源，并明确标注修改。详见 [NOTICE.md](NOTICE.md)。

[English](README_en.md) | [日本語](README_jp.md) | [Português (Brasil)](README_PT-BR.md) | **简体中文**
