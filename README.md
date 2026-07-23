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
| 当前稳定版 | r14.2.9 |
| 上一稳定版 | r14.2.8 |
| 当前候选版 | r14.2.10 |
| 应用名 | 米客 A14 |
| 包名 | `name.monwf.customiuizer.r14` |
| 目标系统 | HyperOS 1 / Android 14 |
| Hook 接口 | libxposed API 101 |
| LSPosed 基线 | [Vector v2.0-3046](https://github.com/JingMatrix/Vector/actions/runs/29805285935)，commit `9350c7c` |
| 发布页 | [tomthenpc/customiuizer-a14 Releases](https://github.com/tomthenpc/customiuizer-a14/releases) |

r14.2.10 已完成构建、签名、单元测试与 `assembleRelease`，为当前候选版；实机完整重启验证待完成后才能标记为稳定。r14.2.9、r14.2.8、r14.2.7 均已通过 LSPosed 后台日志验证，未出现 CustoMIUIzer 相关崩溃或异常，可视为当前推荐稳定版序列。r14.2.4、r14.2.3、r14.2.2、r14.2.1、r14.2.0、r14.1.3 日志验证均无异常，可作为回退基线。r14.2.5 与 r14.2.6 因状态栏过渡尝试导致双排信号图标深浅色切换异常，已回退并删除 tag/release，不再推荐使用。

覆盖安装后、完整重启前，旧 SystemUI 进程可能因热加载新模块产生一次性 Hook 失败记录；完整重启后不再复现，不属于正式启动故障。因此升级模块后必须完整重启设备，不能只重启桌面或 SystemUI。

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

## r14.2.10 优化重点

- 新增 `SystemUI.hasStatusBarModifications()`，无对应功能开启时 `setupStatusBar` 不再调用 `addFakeResource` 和 `setThemeValueReplacement`/`setResReplacement`，仅保留 `systemui_restart_time` 标记写入，降低 SystemUI 启动时无效资源替换开销。
- `WeatherDataController` 改用单一 `ExecutorService` 后台查询天气，避免每分钟 `TIME_TICK` 触发时新建 `Thread`；`Handler` 改为静态 `Looper.getMainLooper()` 实例；`initContext` 重复进入时先注销旧 `TIME_TICK` 接收者并显式使用 `Context.RECEIVER_NOT_EXPORTED`。

## r14.2.9 优化重点

- `StepCounterController.initContext` 静态持有 `TIME_TICK` 接收者，SystemUI 重建时先注销旧接收者再注册新接收者；`Handler` 改用 `mContext.getMainLooper()`；`removeStepViewByTag` 改用 `removeIf` 避免并发修改。
- `BatteryIndicator.updateDisplaySize` 缓存 `mDensity` 与 `mStatusBarHeight`，`updateDrawable` 绘制时不再重复查询 `Resources`。
- `BatteryIndicator.updateParameters` 移除每次调用新建的 `Matrix` 对象。

## r14.2.8 优化重点

- `MainModule` 偏好变化监听不再调用 `sharedPreferences.getAll()` 复制整个远程偏好表，改为按已有值类型单次读取，降低设置调整时的内存与 CPU 开销。
- `GlobalActions.setupStatusBar` 内的 `MiuiFreeformModeController`（PinningWindow，动作码 28）、`SoScSplitScreenController`（SplitScreen，动作码 29）与 `AutoBrightnessController`（ToggleAutoBrightness，toggle 6）Hook 仅在对应动作被配置时才注册，避免任意自定义动作开启时加载这些控制器 Hook。
- 移除 `GlobalActions.mSBReceiver` `OpenVolumeDialog` 分支内被错误嵌套、永远不会执行的 `ToggleZenMode` / `ToggleNightMode` 子分支。

## r14.2.7 优化重点

- `GlobalActions.hasCustomActions()` 集中判断是否存在任何自定义动作；未配置时 `MainModule` 不再调用 `setupGlobalActions` / `setupStatusBar`，避免在 `system_server` 和 `SystemUI` 中注册 `mSBReceiver`、自由窗口/分屏/自动亮度控制器 Hook。
- `MainModule` 仅在 `launcher_privacyapps_gest` 开启时调用 `Launcher.setupLauncher`，避免为所有桌面进程注册隐私应用配置广播接收者。
- `SystemUI` 手电筒与 `Various` 下一闹钟 `ContentObserver` 改为 `new Handler(mContext.getMainLooper())`，不再依赖当前线程 Looper。
- `System.java` 秒级时钟刷新 `TIME_SET` 广播接收者改为先注销旧实例再注册，且仅在启用秒针显示时才注册。

## r14.2.4 优化重点

- `MainModule` 在 `SystemUI` 进程中按开关判断是否存在控制中心/音量相关定制；未开启对应功能时跳过 `ControlCenterPluginHook` 注册，避免加载 `miui.systemui.plugin` 插件 loader 及其后续大量 UI 回调。
- `GlobalActions` 中 `MiuiFreeformModeController`、`SoScSplitScreenController` 与 `AutoBrightnessController` 的 `BroadcastReceiver` 注册前通过 `AdditionalInstanceField` 检查并注销旧实例，重复进入 `onInit`/`构造` 时不再累积多个接收者。
- 保留 r14.2.3 的 `ContentObserver` / `BroadcastReceiver` 生命周期治理、r14.2.2 的反射缓存哨兵、r14.2.1 的热路径缓存与绘制对象复用边界，不扩大未验证的 API 101 原生拦截范围。

## r14.2.0 优化重点

- 偏好键在加载时一次规范化，高频 Hook 读取不再反复拼接 `pref_key_` 或重复查表。
- API 101 兼容层按需生成参数数组；清除 164 处无效/无改写参数复制和 116 次无效对象读取。
- Hook 注册不再为参数签名构造并长期保留重复字符串缓存，兼容层的后置回调检测延迟到首次实际执行。
- 秒级时钟刷新改为主线程单调度，不再额外创建 `Timer` 后台线程和跨线程转发。
- 修复音量面板模糊参数的动态更新键不匹配，并加强偏好观察者的并发安全。
- 保持 r14.1.3 已验证的 API 101 混合架构、SystemUI 空值保护、独立包名与无网络权限边界。

r14.1.3 主要解决线程、缓存、图像处理、音频计算与无关资源占用，属于宏观资源优化；r14.2.0 则继续治理偏好读取、Hook 参数、反射、无效分支和线程调度，重点是减少长期运行热路径上的短期对象、重复计算与跨线程唤醒。r14.2.0 的目标不是明显缩小 APK，而是在不改变已验证兼容边界的前提下进一步收紧执行成本。

### 模块加载实测

以下数据来自同一设备的 Vector/LSPosed 日志，只统计 `name.monwf.customiuizer.r14` 从开始加载到报告成功的时间：

| 版本与启动周期 | 样本数 | 中位数 | 平均值 | 范围 |
|---|---:|---:|---:|---:|
| r14.1.3 第一次启动 | 8 | 11.5 ms | 18.9 ms | 5–51 ms |
| r14.1.3 第二次启动 | 9 | 8 ms | 15.7 ms | 5–37 ms |
| r14.2.0 正式版 | 9 | 8 ms | 22.8 ms | 5–81 ms |

r14.2.0 与稳定后的 r14.1.3 加载中位数相同，日志不能证明模块入口有显著提速。平均值和长尾会受到目标进程启动负载影响；本版收益主要发生在加载完成后的高频 Hook 与长期运行阶段。

## 版本演进与静态对比

| 版本 | APK 大小 | 相对上一版 | 主要变化与性能定位 |
|---|---:|---:|---|
| [上游 v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) | 2,785,364 B | Android 14 基线 | 上游最后阶段的 Android 14 功能参考；API 100，保留联网权限和原版支持资源 |
| [r14.0.0](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.0.0) | 2,901,900 B | +116,536 B | 建立独立 Android 14/API 101 版本线，加入类、方法、参数、资源与主题缓存 |
| [r14.1.0](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.1.0) | 2,901,856 B | −44 B | 建立原生 API 101 `intercept(Chain)` 架构，优先迁移全局操作与控制模块 |
| [r14.1.1](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.1.1) | 2,934,624 B | +32,768 B | 扩大原生拦截迁移范围，并通过实机故障确认 SystemUI 兼容层边界 |
| [r14.1.2](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.1.2) | 2,934,628 B | +4 B | 隔离普通应用启动与 Xposed 类型，恢复稳定混合架构；以可靠性为主 |
| [r14.1.3](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.1.3) | 2,886,250 B | −48,378 B | 有界线程池、受限图标缓存、FFT/Bitmap/GC 优化，并移除支持页面和网络权限；资源收益最大 |
| [r14.2.0](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.0) | 2,886,165 B | −85 B | 优化偏好热路径、Hook 参数、反射、并发可见性与秒钟调度；长期运行细节最完整 |
| [r14.2.1](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.1) | 2,886,165 B | 0 B | 偏好解析缓存、反射 `Optional`→`NOT_FOUND` 哨兵、BatteryIndicator/AudioVisualizer 绘制对象复用 |
| [r14.2.2](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.2) | 2,886,165 B | 0 B | `XposedHelpers` 反射缓存完整迁移为 `NOT_FOUND` 哨兵，减少缓存命中/写回包装对象 |
| [r14.2.3](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.3) | 2,886,165 B | 0 B | ContentObserver / BroadcastReceiver 生命周期治理，减少重复注册与 Handler/Runnable 临时分配 |
| [r14.2.4](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.4) | 2,886,165 B | 0 B | 按开关跳过 `ControlCenterPluginHook` 注册，治理 `BroadcastReceiver` 重复注册，减少功能关闭时的无效 Hook |
| [r14.2.10](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.10) | 2,886,165 B | 0 B | `SystemUI.setupStatusBar` 按 `hasStatusBarModifications()` 跳过无效资源替换；`WeatherDataController` 统一后台执行器并修复接收者生命周期 |
| [r14.2.9](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.9) | 2,886,165 B | -4 B | 修复 `StepCounterController` 接收者生命周期；`BatteryIndicator` 绘制热路径缓存 density/statusbar 高度并减少 Matrix 分配 |
| [r14.2.8](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.8) | 2,886,169 B | +4 B | 偏好监听避免 `getAll()` 全量复制；按具体动作码 gate 控制器子 Hook；清理 `OpenVolumeDialog` 内失效嵌套分支 |
| [r14.2.7](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.7) | 2,886,165 B | 0 B | 按自定义动作开关跳过 `GlobalActions` 接收者与控制器 Hook；治理 `ContentObserver` / `TIME_SET` 接收者生命周期 |

r14.2.0 比 r14.0.0 小 15,735 B，比 r14.1.2 小 48,463 B；相对上游 v24.10.12 大 100,801 B（约 3.62%）。主要体积差异来自 API 101 原生运行库：上游基线约为 290,440 B，本项目 API 101 库为 381,024 B，单项增加约 90.6 KB。APK 大小并不等同于运行效率。

上游最新的 [v25.09.25](https://github.com/MonwF/customiuizer/releases/tag/v25.09.25) 已转向 Android 15 / HyperOS 2、目标 SDK 35，不能与本项目在 Android 14 上直接进行运行性能比较；本项目的有效上游基线仍是 v24.10.12。

综合来看，r14.1.3 是实际资源治理幅度最大的一版，r14.2.0 是热路径和并发细节最完整的一版，r14.1.2 则是关键稳定性节点。具体耗电改善仍需在相同设备、相同功能开关和使用场景下通过 Perfetto 或 Batterystats 长时间对照，不能仅凭 APK 大小或 LSPosed 加载日志量化。

完整版本记录见 [CHANGELOG.md](CHANGELOG.md)。

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
