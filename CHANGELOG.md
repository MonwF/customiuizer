# 更新日志

非官方优化 fork，仅兼容 **HyperOS 1 / Android 14 / libxposed API 101**。

## r14.1.2 · 2026-07-22

### 紧急修复：重启后 Hook 生效，但应用无法打开

- LSPosed `full.log` 显示崩溃发生在模块普通应用进程 `name.monwf.customiuizer.r14`，早于 Activity 创建：`androidx.startup.InitializationProvider` 初始化时抛出 `NoClassDefFoundError: io.github.libxposed.api.XposedInterface$Hooker`。因此问题不在系统进程 Hook 注册，而在设置应用自身的启动路径。
- 对比 DEX 可见，r14.1.1 的 `EmojiCompatInitializer`、`ProcessLifecycleInitializer`、`ProfileInstallerInitializer` 均保留 `androidx.startup.Initializer` 接口；首个 r14.1.2 构建中该接口关系被 R8 优化掉，`AppInitializer` 也参与了横向类合并，使普通应用进程错误触发仅应由 LSPosed 提供的 API 类型解析。
- 新增 R8 隔离规则，完整保留 `androidx.startup` 及全部 `Initializer` 实现，阻止启动组件与 Xposed 专用类合并。修复后映射和 DEX 均恢复明确的 `androidx.startup.Initializer` 接口；检查 11 个 Startup/初始化器类区段，未发现 `libxposed` 或 `XposedInterface$Hooker` 引用。
- `io.github.libxposed:api` 继续使用 `compileOnly`，不把框架 API 塞入 APK，避免普通应用虽能启动、但 Hook 进程因重复 API 类而出现类加载冲突。

### Hook 与设置稳定性

- 保留 r14.1.1 已验证的 `SystemUI` 兼容适配层，不合入曾导致重启后 Hook 失效的完整迁移方案。
- Xposed 服务连接时，将应用侧设置完整同步到 LSPosed Remote Preferences；日常修改仍按键增量同步，确保系统进程下次启动能读取完整配置。
- 不新增开机广播、后台服务、定时任务或持续轮询。
- 修复隐藏 API 调用、动态广播注册、Wi-Fi 扫描和多处控制流的静态检查问题。

### 性能与代码质量

- 四个应用列表共用可自动回收的 2–4 线程图标加载池，替代每个列表各自常驻的 `CPU + 1` 线程池。
- 图标内存缓存由运行时最大堆的 50% 调整为约 12.5%，并限制在 1–16 MiB；移除主动 `Runtime.gc()`。
- 缓存 WindowManager 服务及动画缩放反射方法，减少重复 Binder 查询和反射解析。
- 反射查询复用键在每次查找后清空类与参数引用，降低长期持有系统 ClassLoader 的风险。
- 移除常量 Hook 回调的类型不安全全局缓存，保留无状态的空回调快速路径。
- 音频可视化 FFT 每帧只扫描一次，静音输入不再对每个频段重复扫描；专辑图比较改为引用判定，避免 UI 线程逐像素比较。
- 收敛异常日志和重复辅助代码，移除废弃页面、资源与无引用字符串。

### 轻量化

- 移除应用“支持”区域中的版本下载、代码仓库、联系方式与赞赏入口。
- 删除内置网页页、赞赏布局及 73,437 字节赞赏图片。
- 删除不再需要的 `INTERNET` 权限；保留 Hook 与设置功能所需权限。

### 构建与评估

| 指标 | r14.1.1 | r14.1.2 | 变化 |
|---|---:|---:|---:|
| APK | 2,934,624 B | 2,853,094 B | -81,530 B（-2.78%） |
| 压缩后 DEX | 1,230,528 B | 1,228,584 B | -1,944 B（-0.16%） |
| 压缩后 `res/` | 387,843 B | 313,555 B | -74,288 B |
| 压缩后资源表 | 819,820 B | 812,944 B | -6,876 B |
| 压缩后原生库 | 381,024 B | 381,024 B | 不变 |

- 相比首个会崩溃的 r14.1.2 构建，启动隔离修复增加 16,384 B APK 体积（+0.58%）和 3,136 B 压缩 DEX；这是为恢复可验证启动边界而保留的稳定性成本，最终包仍比 r14.1.1 小 81,530 B。
- `clean check assembleRelease` 通过；APK 通过 zipalign 与 apksigner 校验，仅启用 v2 签名，签名证书与 r14.1.1 一致。
- 清单验证：versionCode 116、versionName r14.1.2、min/target SDK 34，未声明 `INTERNET`。
- R8 映射验证：`AppInitializer`、`InitializationProvider` 和三个清单初始化器均保持独立；修复包 DEX 中三个初始化器均直接实现 `androidx.startup.Initializer`。

### 相对 r14.1.1 的性能评估

| 路径 | r14.1.1 | r14.1.2 | 预期影响 |
|---|---|---|---|
| 应用列表图标加载 | 四个列表各自持有 `CPU + 1` 线程池 | 共用可回收的 2–4 线程池 | 降低同时打开/切换列表时的线程数、栈内存与调度开销；8 核设备理论上限由 36 个工作线程降至 4 个 |
| 图标缓存 | 最大堆的 50% | 约 12.5%，并限制为 1–16 MiB | 降低设置应用占用和系统内存压力，不再主动触发 `Runtime.gc()` |
| 系统服务与反射 | 重复查询 WindowManager、重复解析方法 | 缓存服务和稳定反射入口 | 减少 Binder 查询、反射查找和临时对象 |
| 音频可视化 | 每个频段重复扫描 FFT 数据 | 每帧单次前向扫描 | 降低可视化启用时的逐帧 CPU 计算，静音输入提前结束 |
| 后台行为 | 无新增常驻任务 | 仍无开机广播、服务、轮询或定时任务 | 本轮优化不会用额外后台唤醒换取前台速度 |
| 安装包资源 | 保留支持页面、网页和赞赏资源 | 删除相关入口与资源 | APK 减少 2.78%，资源解压与安装占用同步下降 |

- 上述为构建产物、线程上限和代码热路径的结构化评估，不等同于真机耗电/续航跑分。当前无 ADB 设备；用户日志已确认故障包重启后 Hook 可生效，修复包仍需在目标 HyperOS 1 ROM 上验证“打开应用、读取设置、修改设置、完整重启、关键 Hook”闭环。

## r14.1.1

- `Launcher`、`System`、`Various` 完成原生 `intercept(Chain)` 迁移并通过当时的重启验证。
- `SystemUI` 完整迁移在重启后失效，因此回退并固定为 `HookerClassHelper` 兼容适配层。
- 最终原生迁移范围：`GlobalActions`、`Controls`、`Launcher`、`System`、`Various`。
- `HookBuilder` 显式使用 `ExceptionMode.PASSTHROUGH`。
- versionCode 115；正式 APK 2,934,624 B，v2 签名。

## r14.1.0

- 建立原生 libxposed API 101 实现，`MethodHook` 直接实现 `XposedInterface.Hooker`。
- 首先迁移 `GlobalActions` 与 `Controls`，保留可变参数、提前返回、结果替换及异常传播语义。
- 其他模块暂经兼容适配层运行，按模块逐步迁移。
- versionCode 109；正式 APK 2,901,856 B。

## r14.0.0

- 将模块生命周期和 Hook 注册迁移至 libxposed API 101。
- 限定 Android 14，避免作用于未适配的 Android 15/16 系统组件。
- 保留旧回调模型的兼容适配实现，为后续分模块迁移提供稳定基线。
- versionCode 108。

## 安装提醒

升级前先备份设置；不要与官方版或其他 fork 同时启用。安装或修改关键 Hook 后，应完整重启设备并保留上一版 APK 作为回退方案。
