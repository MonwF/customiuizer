# 更新日志

非官方优化 fork，仅兼容 **HyperOS 1 / Android 14 / libxposed API 101**。

## r14.1.2 · 2026-07-22

### 稳定性

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
| APK | 2,934,624 B | 2,836,710 B | -97,914 B（-3.34%） |
| 压缩后 DEX | 1,230,528 B | 1,225,448 B | -5,080 B |
| 压缩后 `res/` | 387,843 B | 313,555 B | -74,288 B |
| 压缩后资源表 | 819,820 B | 812,944 B | -6,876 B |

- `clean assembleRelease` 与 `lintRelease` 均通过。
- APK 通过 zipalign 与 apksigner 校验，仅启用 v2 签名；签名证书与 r14.1.1 一致。
- 清单验证：versionCode 116、versionName r14.1.2、min/target SDK 34，未声明 `INTERNET`。
- 本表是构建产物和代码路径评估，不代表真机续航跑分。本轮无已连接设备，发布后仍需在目标 ROM 完整重启并验证关键 Hook。

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
