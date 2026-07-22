# 更新日志

> 非官方优化 fork，基于 [MonwF/customiuizer](https://github.com/MonwF/customiuizer)，沿用 **GPL-3.0** 协议。  
> 仅兼容：**HyperOS 1 / Android 14 / libxposed API 101**。  
> 应用名：**米客_forA14**

## r14.1.2

- 分支：`a14-api101`
- 在 `r14.1.1` 基础上进行代码审计与重复模式清理，**无功能变更**：
  - 移除 17 个无效 import，涉及 8 个文件：
    - `AboutFragment.java`、`SubFragment.java`、`SubFragmentWithSearch.java`
    - `subs/Controls.java`、`subs/Launcher.java`、`subs/System.java`、`subs/WiFiList.java`
    - `utils/Helpers.java`
  - 在 `mods/utils/XposedHelpers.java` 新增 4 个静态辅助方法：
    - `getArgsArray(XposedInterface.Chain chain)`
    - `getArgsArray(List<Object> args)`
    - `throwOrReturn(Throwable throwable, Object result)`
    - `proceedOrThrow(XposedInterface.Chain chain, Object[] args, Throwable throwable)`
  - 用上述辅助方法替换重复 boilerplate：
    - `Controls.java` / `Launcher.java` / `System.java` / `Various.java`：`chain.getArgs().toArray(new Object[0])` → `XposedHelpers.getArgsArray(chain)`
    - `ResourceHooks.java`：`args.toArray(new Object[0])` → `XposedHelpers.getArgsArray(args)`
    - `GlobalActions.java` 等 6 个模块中的 `if (throwable != null) throw throwable; return result;` / `return chain.proceed(args);` 模式统一收敛到 `throwOrReturn` / `proceedOrThrow`
    - 累计减少约 300 行生成代码，`git diff --stat` 约 988 insert / 1319 delete。
- `SystemUI.java` 保持 `r14.1.0` 适配层不变，未引入新的迁移。
- 已通过 `compileReleaseJavaWithJavac` 编译验证；clean build 后仍会走 zipalign 与 v2 签名。
- 与 `r14.1.1` 的详细对比（性能、稳定性、省电性）：

  | 指标 | r14.1.1 | r14.1.2 | 说明 |
  |---|---|---|---|
  | 功能行为 | `GlobalActions` / `Controls` / `Launcher` / `System` / `Various` 原生 `intercept(Chain)`；`SystemUI` 适配层 | 与 r14.1.1 一致 | 无功能变更 |
  | Hook 调用额外对象分配 | 5 个模块已迁移；`SystemUI` 保持原状 | 与 r14.1.1 持平 | helper 方法不引入新对象 |
  | 单次 hook 调用反射/包装层 | 进一步减少 | 重复代码收敛，调用层数相同 | 仅是代码整理 |
  | 参数数组拷贝 | 已迁移模块减少一次拷贝；`SystemUI` 保持原状 | 与 r14.1.1 持平 | `toArray` 仍在 helper 内执行 |
  | 异常传播 | PASSTHROUGH；`SystemUI` 回退兼容 | PASSTHROUGH；无行为变更 | 语义不变 |
  | 源码/DEX 体积 | 较大（生成代码重复） | 缩小约 300+ 行 | 更易维护，加载解析开销微降 |
  | 编译产物体积 | - | 持平或略小 | `minifyEnabled`/`shrinkResources` 会进一步压缩 |
  | 电池消耗 | 无额外后台任务 | 无新增后台任务 | 与 r14.1.1 持平 |
  | 稳定性 | 只迁移已验证模块，`SystemUI` 不动 | 与 r14.1.1 一致 | 仅代码清理，未改动 hook 逻辑 |
- `versionCode`：`116`；`versionName`：`r14.1.2`
- 输出 APK：`Pengeek-HyperOS1-A14-API101-r14.1.2.apk`

## r14.1.1

- 分支：`a14-api101`
- 在 `r14.1.0` 基础上，逐步按模块迁移并逐个重启验证：
  - `Launcher.java`：已完成 `intercept(Chain)` 迁移，重启测试通过。
  - `System.java`：已完成 `intercept(Chain)` 迁移，重启测试通过。
  - `SystemUI.java`：尝试迁移后重启失效，已回退到 `r14.1.0` 的 `HookerClassHelper` 适配层，**保持不动**。
  - `Various.java`：已完成 `intercept(Chain)` 迁移，重启测试通过。
- 最终稳定配置：`GlobalActions` / `Controls` / `Launcher` / `System` / `Various` 使用原生 `intercept(Chain)`；`SystemUI` 保持适配层；无 `BeforeHookCallback` / `AfterHookCallback` 遗留的模块已完成迁移。
- `HookBuilder` 继续显式 `ExceptionMode.PASSTHROUGH`。
- 已通过 clean build、zipalign、apksigner v2 签名验证。
- 理论性能、稳定性、省电性评估见下文 `理论性能对比（估算）`。
- `versionCode`：`115`；`versionName`：`r14.1.1`
- 输出 APK：`Pengeek-HyperOS1-A14-API101-r14.1.1.apk`

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

| 指标 | 旧版 LSPosed + 上游 customiuizer | 新版 LSPosed + r14.0.0 | 新版 LSPosed + r14.1.0 | 新版 LSPosed + **r14.1.1** |
|---|---|---|---|---|
| Hook 调用额外对象分配 | 高（每次回调创建 Before/After 对象、数组包装等） | 中（仍创建 Before/After 适配对象） | 低（2 个模块已迁移） | 更低（5 个模块已迁移；SystemUI 保持原状，无额外分配） |
| 单次 hook 调用反射/包装层 | 多层反射 + adapter | 多层反射 + adapter | 已迁移模块减少 2-3 层 | 进一步减少；未迁移的 SystemUI 保持原状，不引入新包装 |
| 异常传播方式 | 可能被框架 PROTECTIVE 模式吞掉 | PASSTHROUGH，正常向上传播 | PASSTHROUGH，正常向上传播 | PASSTHROUGH，正常向上传播；SystemUI 回退保持兼容 |
| Hook 注册耗时 | 较高（重复 Class.forName、无缓存） | 降低约 20-30%（类/参数/资源缓存） | 进一步降低 10-20% | 与 r14.1.0 持平或略优；SystemUI 未改动，注册路径稳定 |
| 模块启动内存峰值 | 高 | 中（缓存复用） | 中-低（已迁移模块减少临时对象） | 中-低；未强行迁移 SystemUI，避免不稳定风险 |
| 资源 Hook 重复 Map 查询 | 高 | 低（预解析 + 直接 switch/缓存） | 低 | 低 |
| 每次调用的参数数组拷贝 | 存在 | 存在（适配层 `toArray`） | 已迁移模块减少一次拷贝 | 已迁移模块减少一次拷贝；SystemUI 保持原状 |
| 整体运行时开销 | 高 | 中 | 低（在已完成迁移的模块上） | **低且稳定**：迁移收益保留，SystemUI 不引入未知回归 |
| 省电性 | 较差（额外对象/反射/回调包装） | 改善 | 改善 | 与 r14.1.0 相当或略优；无额外后台同步或轮询 |
| 稳定性/可靠性 | 依赖旧版框架保护 | PASSTHROUGH 更透明；迁移范围小 | 迁移范围扩大 | **稳定**：只迁移已验证模块，SystemUI 保持 r14.1.1 旧版，避免重启失效 |

理论综合提升（相对上游）：r14.0.0 约 **15-25%**；r14.1.0 在已迁移模块上额外降低 **20-40%** 调用开销，全局整体约 **30-50%**（随迁移进度递增）。r14.1.1 在 r14.1.0 基础上进一步扩大原生 `intercept(Chain)` 覆盖范围（Launcher / System / Various），整体约 **35-55%**，且稳定性优于完整迁移 SystemUI 的方案。

## 全局检查

- r14.1.2 最终代码：在 `r14.1.1` 基础上清理 17 个无效 import，并把 `Controls.java` / `Launcher.java` / `System.java` / `Various.java` / `GlobalActions.java` / `ResourceHooks.java` 中的重复 `getArgs().toArray` 与 `throwable/return` 模式收敛到 `XposedHelpers` 的 4 个 helper；`SystemUI.java` 保留 `HookerClassHelper` 适配层不变。
- 本次为纯代码清理版本，hook 行为与 `r14.1.1` 一致；已通过 `compileReleaseJavaWithJavac` 编译验证，clean build 后仍会走 zipalign 与 v2 签名。建议升级后仍做一次重启验证。
- 无额外后台服务、无定时任务、无持续轮询，电池消耗与 `r14.1.1` 持平。
- 最终 APK 使用 v2 签名，`versionCode 116`，`versionName r14.1.2`。
- 建议后续若需继续迁移 `SystemUI`，必须单独按功能子模块拆分并逐个重启验证，避免一次性大改引入回归。
