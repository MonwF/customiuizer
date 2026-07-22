# 更新日志

非官方优化 fork，基于 [MonwF/customiuizer](https://github.com/MonwF/customiuizer)，沿用 GPL-3.0 协议。仅兼容 **HyperOS 1 / Android 14 / libxposed API 101**。

## r14.1.3 — 轻量化、启动隔离与资源治理

基线：用户确认可正常打开、重启后可 Hook 的 Devin r14.1.2 恢复版。此次新建 r14.1.3，不再覆盖 r14.1.2。

### 轻量化

- 移除主界面和关于页中的以下支持内容：
  - 版本下载
  - 代码仓库
  - 微信与 PayPal 赞赏
- 删除只为上述功能服务的代码和资源：
  - 内置 `WebView` 页面及布局
  - 赞赏图片及布局
  - “在浏览器中打开”菜单
  - 11 种语言中的废弃支持文案
- 移除应用自身不再需要的 `INTERNET` 权限。
- 保留 `RECEIVE_BOOT_COMPLETED`、蓝牙、Wi‑Fi、跨用户和模块运行所需权限。

### 启动与 Hook 稳定性

- 保持 r14.1.2 的 Hook 模块边界和调用语义，不进行 Java→Kotlin 或 SystemUI 全量迁移。
- 调整 R8 规则，禁止 `XposedModule` 与 `XposedInterface.Hooker` 实现被优化合并进普通应用或 AndroidX Startup 类。
- 最终 DEX 已静态检查：`InitializationProvider` 启动路径不直接依赖 libxposed；Hooker 类型仍只在模块回调类中实现。
- 修复 Android 14 动态广播注册标志，侧边栏广播明确使用 `RECEIVER_EXPORTED`。

### 性能与内存

- 图标加载：
  - r14.1.2：4 类应用列表分别创建线程池；每个池的核心线程为 `CPU + 1`，最大线程为 `2 × CPU + 1`，队列无界。
  - r14.1.3：统一为 1 个共享池，固定 2–4 个后台优先级线程，空闲 15 秒回收，等待队列上限 128。
  - 收益方向：降低快速滚动或反复进入应用选择页时的线程竞争、任务积压和唤醒次数。
- 图标缓存：
  - 上限由“Java 最大堆的 1/2”改为 1–16 MiB 的有界 LRU。
  - 删除内存不足分支中的主动 `Runtime.gc()`，避免人为触发停顿。
- 音频可视化：
  - 静音 FFT 判断由“每帧每频段扫描一次”改为“每帧扫描一次”。31 频段静音帧的判断扫描次数由最多 31 次降为 1 次。
  - 专辑图重复检查由主线程逐像素 `Bitmap.sameAs()` 改为对象身份判断，避免 UI 线程大图比较。

### 可靠性与代码质量

- Wi‑Fi 扫描结果读取增加 `SecurityException` 兜底，权限被拒时不再直接崩溃。
- 补齐 Activity 回调父类调用。
- 为条件、循环和分支补齐括号，清除歧义缩进。
- MIUI 私有服务名与隐藏音量标志保留原值，并增加局部说明；不修改兼容行为。
- `lintRelease`：由基线 27 个错误降为 0；现存 429 个警告主要为私有 API、旧版布局和未使用资源提示，不影响 Release 构建。

### 与 r14.1.2 的评估

| 指标 | r14.1.2 稳定恢复版 | r14.1.3 | 结论 |
|---|---:|---:|---|
| APK 大小 | 2,934,628 B | 2,885,842 B | 减少 48,786 B（1.66%） |
| 支持/下载/赞赏界面 | 有 | 已移除 | UI 更精简，无内置网页 |
| 应用 `INTERNET` 权限 | 有 | 无 | 减少普通应用网络能力 |
| 图标加载线程池 | 每个 Adapter 独立 | 单个 2–4 线程共享池 | 降低并发与排队上限 |
| 图标缓存上限 | 最大堆的 1/2 | 1–16 MiB | 降低内存峰值与 GC 压力 |
| 主动 GC | 低内存时调用 | 已移除 | 减少卡顿风险 |
| 静音 FFT 检查 | 每频段一次 | 每帧一次 | 降低可视化空闲计算 |
| Lint 错误 | 27 | 0 | 运行时边界更明确 |
| Hook 架构 | 已验证混合架构 | 保持不变 | 不扩大兼容风险 |

> 性能与省电结论基于代码路径、线程上限和对象生命周期分析，不是实机功耗跑分。r14.1.3 没有新增服务、定时任务或持续轮询；预期收益主要出现在应用列表、音频可视化静音帧和内存压力场景。最终仍应以目标设备的冷启动、重启 Hook 和日常待机测试为准。

### 构建与产物

- `versionCode`：117
- `versionName`：r14.1.3
- APK：`Pengeek-HyperOS1-A14-API101-r14.1.3.apk`
- Release：R8 + `shrinkResources` + zipalign + v2 签名
- 已执行：`clean assembleRelease`、`lintVitalRelease`、`lintRelease`、签名/证书检查和 DEX 启动路径检查

## r14.1.2 — 稳定恢复基线

- `versionCode`：116；`versionName`：r14.1.2。
- 保持 r14.1.1 的模块组合：
  - `GlobalActions`、`Controls`、`Launcher`、`System`、`Various` 使用原生 `intercept(Chain)`。
  - `SystemUI` 保留 `HookerClassHelper` 适配层。
- 收敛重复的参数数组、异常传递和返回值处理代码，不改变 Hook 语义。
- 用户确认的稳定恢复 APK：2,934,628 B；SHA-256：`a46acee41da42c618ee0f23468bb37574faedbfb4f9a5df6b26b678106dd32ea`。

## r14.1.1 — 分模块 API 101 迁移

- `Launcher`、`System`、`Various` 逐模块迁移到 `intercept(Chain)` 并分别验证。
- `SystemUI` 迁移后曾出现重启失效，因此回退并保留 r14.1.0 适配层。
- 最终稳定组合沿用至 r14.1.2 和 r14.1.3。
- `HookBuilder` 显式使用 `ExceptionMode.PASSTHROUGH`。

## r14.1.0 — 原生 API 101 起点

- `MethodHook` 直接实现 `XposedInterface.Hooker`，使用 `intercept(Chain)` 调度。
- 首批迁移 `GlobalActions` 与 `Controls`。
- 保留参数修改、提前返回、异常传播与 after 回调语义。

## 安装提示

安装前请备份设置，并卸载官方版或其他 fork。启用模块和作用域后必须重启设备；请重点验证设置应用能否打开、SystemUI、桌面、锁屏和常用 Hook 功能。
