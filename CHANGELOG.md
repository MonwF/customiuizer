# 更新日志

本文件只记录 CustoMIUIzer A14 独立版本线。上游历史请查阅 [MonwF/customiuizer Releases](https://github.com/MonwF/customiuizer/releases)。性能结论分为静态分析与实机验证，不使用未经测量的百分比。

## r14.1.3 — Hook 修复、轻量化与资源治理

状态：**修复候选，等待目标设备实机确认**。GitHub 上 2026-07-22 发布的首个 r14.1.3 预发布包存在后置 Hook 回归，暂不应作为稳定版使用；r14.1.2 未被覆盖。

### 修复

- 修复 Release 混淆后全部 `after` Hook 被跳过的问题：R8 会重命名回调方法，而旧适配层通过硬编码方法名检测回调是否存在。
- `MethodHook` 现在按返回类型和参数类型识别后置回调，不再依赖方法名，可兼容 R8 重命名。
- 恢复依赖后置回调的路径，包括：
  - Launcher `Application.attach` 后的模块初始化
  - 最近任务背景模糊与清理按钮控制
  - 控制中心运营商名称隐藏
  - 控制中心主题与图标颜色更新
- 保留 Hooker 与普通 AndroidX 启动链的 R8 隔离，防止再次出现“重启后 Hook 生效，但设置应用打不开”。
- Android 14 动态广播明确设置导出标志。
- Wi-Fi 扫描结果读取增加权限拒绝兜底。

### 精简

- 移除“支持”区域中的版本下载、代码仓库、微信与 PayPal 赞赏入口。
- 删除仅服务于上述入口的 WebView 页面、布局、图片、菜单和多语言文案。
- 移除应用的 `INTERNET` 权限；保留开机广播、蓝牙、Wi-Fi、跨用户和 Hook 所需权限。
- 删除仓库中的上游赞助配置与已失效的 Crowdin 工作流。

### 性能与内存

- 将 4 类应用列表各自创建的线程池合并为单个共享池，固定 2–4 个后台线程，空闲 15 秒回收，等待队列上限 128。
- 图标 LRU 缓存由“最多占 Java 堆的一半”改为 1–16 MiB 的有界缓存。
- 删除低内存分支中的主动 `Runtime.gc()`，避免人为触发停顿。
- 音频可视化的静音 FFT 判断由每频段一次改为每帧一次；31 频段静音帧最多从 31 次扫描降为 1 次。
- 专辑图重复检查不再在主线程执行逐像素 `Bitmap.sameAs()`。

### 工程规范

- 项目名统一为 **CustoMIUIzer A14**，应用显示名统一为 **米客 A14**。
- APK 命名统一为 `CustoMIUIzer-A14-<version>.apk`。
- 重写中、英、日、葡 README，移除 HyperOS 2、EdXposed、旧赞赏和旧问题反馈说明。
- 新增 `NOTICE.md`，明确下游身份、来源、修改和 GPL-3.0 分发义务。
- 重写隐私说明，使其与“无网络权限、无遥测、无内置上报”的实际代码一致。

### 与 r14.1.2 的静态比较

| 指标 | r14.1.2 稳定恢复版 | r14.1.3 修复候选 | 结论 |
|---|---:|---:|---|
| APK 大小 | 2,934,628 B | 2,886,250 B | 减少 48,378 B（1.65%） |
| 应用网络权限 | 有 | 无 | 应用本身不能联网 |
| 图标执行器 | 每个 Adapter 独立、队列无界 | 单个 2–4 线程共享池、队列 128 | 限制线程竞争和积压 |
| 图标缓存 | 最大堆的 1/2 | 1–16 MiB | 降低内存峰值 |
| 主动 GC | 低内存时调用 | 已移除 | 降低卡顿风险 |
| 静音 FFT 判断 | 每频段一次 | 每帧一次 | 减少空闲计算 |
| Lint 错误 | 27 | 0 | 构建边界更明确 |
| Hook 架构 | 已验证混合架构 | 保持相同边界 | 不扩大兼容风险 |

这些结果来自代码路径、资源和构建产物分析，不等同于实机功耗跑分。没有新增后台服务、定时任务或持续轮询；实际耗电、启动时间和兼容性仍以同设备、同 ROM、同作用域的对照测试为准。

### 验证状态

- 已通过 `clean assembleRelease lintRelease lintVitalRelease`。
- 已通过 zipalign、APK v2 签名和证书一致性检查。
- 已静态确认 AndroidX `InitializationProvider` 不直接引用 libxposed 类型。
- 已静态确认混淆后的 `MethodHook` 使用签名扫描，不再包含硬编码 `after` 方法名。
- 候选 APK：`CustoMIUIzer-A14-r14.1.3.apk`，SHA-256 `e0ee3cafd9ed50cd0a090e28c2be015bdaf96236c438589a6a892ca1825d042e`。
- 待实机复验：应用冷启动、完整重启、Launcher、SystemUI、锁屏及常用 Hook。

## r14.1.2 — 稳定恢复基线

发布日期：2026-07-22。

- 恢复到用户确认能够打开应用且重启后正常 Hook 的 Devin 最终构建基线。
- 保持 r14.1.1 的模块组合：
  - `GlobalActions`、`Controls`、`Launcher`、`System`、`Various` 使用原生 `intercept(Chain)`。
  - `SystemUI` 保留 `HookerClassHelper` 兼容层。
- `versionCode` 116，APK 2,934,628 B。
- SHA-256：`a46acee41da42c618ee0f23468bb37574faedbfb4f9a5df6b26b678106dd32ea`。

## r14.1.1 — 分模块 API 101 迁移

发布日期：2026-07-21。

- `Launcher`、`System`、`Various` 分模块迁移至 `intercept(Chain)`，并逐步重启验证。
- `SystemUI` 全量迁移后曾出现重启失效，因此回退到兼容层；该边界沿用至后续版本。
- `HookBuilder` 显式使用 `ExceptionMode.PASSTHROUGH`，保留被 Hook 方法的异常传播语义。
- 完成 clean build、zipalign 和 APK v2 签名验证。

## r14.1.0 — 原生 API 101 起点

发布日期：2026-07-20。

- `MethodHook` 直接实现 `XposedInterface.Hooker`，以 `intercept(Chain)` 调度。
- 首批迁移 `GlobalActions` 与 `Controls`。
- 保留可变参数、提前返回、结果替换、异常传播和后置回调语义。
- 其余模块暂时通过 `HookerClassHelper` 兼容层运行。

## r14.0.0 — Android 14 独立版本线

发布日期：2026-07-20。

- 从上游 Android 14 源码建立 libxposed API 101 版本线。
- Hook 初始化限制为 Android 14，避免向不兼容的 Android 15/16 系统组件注册。
- 将包名调整为 `name.monwf.customiuizer.r14`，与上游安装包区分。
- 早期 r3–r14 优化包括类与参数缓存、Context/资源复用、主题值预解析、常量 Hook 快速路径、依赖实例缓存和资源 Hook 早退。

## 安装与回退原则

- 安装前备份设置，不同时启用两个同源模块。
- 每次升级后先打开应用，再完整重启设备。
- 候选版出现异常时回退到最近一个用户确认版本，不覆盖稳定版标签和产物。
- Release 说明必须列出包名、版本号、哈希、签名方案、验证范围和已知限制。
