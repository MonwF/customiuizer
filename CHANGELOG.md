# 更新日志

> 非官方优化 fork，基于 [MonwF/customiuizer](https://github.com/MonwF/customiuizer)，沿用 **GPL-3.0** 协议。  
> 仅兼容：**HyperOS 1 / Android 14 / libxposed API 101**。  
> 应用名：**米客_forA14**

## r14.1.0

- 分支：`a14-api101`
- 原生 API-101 实现：`MethodHook` 直接实现 `XposedInterface.Hooker`，使用 `intercept(Chain)` 调度。
- 已完成 `GlobalActions.java` 与 `Controls.java` 的 `before` / `after` 回调迁移（两模块已无遗留 `BeforeHookCallback` / `AfterHookCallback`）。
- `Launcher.java`、`System.java`、`SystemUI.java`、`Various.java` 仍通过 `HookerClassHelper` 适配层运行，将在后续 r14.1.x 小版本中继续迁移。
- 保留 `before` / `after` 语义：可变参数、提前返回/抛出、结果替换、异常恢复。
- `HookBuilder` 显式 `ExceptionMode.PASSTHROUGH`，确保被 hook 方法自身异常正常向上传播。
- `versionCode`：`109`；`versionName`：`r14.1.0`
- 输出 APK：`Pengeek-HyperOS1-A14-API101-r14.1.0.apk`

## r14.0.0

- 分支：`a14`
- 生命周期与 hook 注册迁移到 **libxposed API 101**。
- 保留 API-100 兼容实现（通过 `HookerClassHelper` 适配 `BeforeHookCallback` / `AfterHookCallback`）。
- 限制 Android 14（`UPSIDE_DOWN_CAKE`），避免 hook 应用到不兼容的 Android 15/16 组件。
- `versionCode`：`108`；`versionName`：`r14.0.0`
- 输出 APK：`Pengeek-HyperOS1-A14-API101-r14.0.0.apk`

## 早期迭代（r3–r13）

类缓存、Context 缓存、资源句柄复用、主题值预解析、资源值直接分发、UserId 直接计算、依赖实例缓存、零参数调用重载、常量 Hook 快速路径、参数类缓存、模块资源配置缓存、跳过空 after 回调、资源 Hook 早退。

## 安装说明

安装本 fork 前请先在 **米客_forA14** 应用内备份设置，然后卸载官方原版并安装本 fork；请勿与官方版或其他 fork 同时启用，否则会导致冲突。

## 构建 / 校验

- 已使用 `zipalign` 与 `apksigner` 验证签名。
- 测试设备：小米 13（HyperOS 1.0.7.0.UMCTWXM，Android 14），r3–r14 均正常重启并加载。

## 理论性能对比（估算）

> 以下数据为基于代码路径和优化点的理论估算，非真机跑分，仅供横向参考。

| 指标 | 旧版 LSPosed + 上游 customiuizer | 新版 LSPosed + r14.0.0 | 新版 LSPosed + r14.1.0 |
|---|---|---|---|
| Hook 调用额外对象分配 | 高（每次回调创建 Before/After 对象、数组包装等） | 中（仍创建 Before/After 适配对象） | 低（已迁移模块直接 `intercept(Chain)`，无适配对象） |
| 单次 hook 调用反射/包装层 | 多层反射 + adapter | 多层反射 + adapter | 已迁移模块减少 2-3 层适配调用 |
| 异常传播方式 | 可能被框架 PROTECTIVE 模式吞掉 | PASSTHROUGH，正常向上传播 | PASSTHROUGH，正常向上传播 |
| Hook 注册耗时 | 较高（重复 Class.forName、无缓存） | 降低约 20-30%（类/参数/资源缓存） | 进一步降低 10-20%（减少 adapter 注册包装） |
| 模块启动内存峰值 | 高 | 中（缓存复用） | 中-低（已迁移模块减少临时对象） |
| 资源 Hook 重复 Map 查询 | 高 | 低（预解析 + 直接 switch/缓存） | 低 |
| 每次调用的参数数组拷贝 | 存在 | 存在（适配层 `toArray`） | 已迁移模块减少一次拷贝 |
| 整体运行时开销 | 高 | 中 | 低（在已完成迁移的模块上） |

理论综合提升（相对上游）：r14.0.0 约 **15-25%**；r14.1.0 在已迁移模块上额外降低 **20-40%** 调用开销，全局整体约 **30-50%**（随迁移进度递增）。

## 全局检查

- r14.1.0 当前代码：`GlobalActions.java`、`Controls.java` 已完成 `intercept(Chain)` 迁移；`Launcher.java`（99 处）、`System.java`（176 处）、`SystemUI.java`（151 处）、`Various.java`（49 处）仍保留 `BeforeHookCallback` / `AfterHookCallback` 适配。
- 已迁移模块在真机测试通过（小米 13，HyperOS 1 A14）。
- 后续 r14.1.x 将按模块继续迁移，直到全部使用原生 API-101。
