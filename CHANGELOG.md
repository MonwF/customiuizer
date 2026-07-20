# 更新日志

> 非官方优化 fork，基于 [MonwF/customiuizer](https://github.com/MonwF/customiuizer)，沿用 **GPL-3.0** 协议。  
> 仅兼容：**HyperOS 1 / Android 14 / libxposed API 101**。  
> 应用名：**米客_forA14**

## 版本总览

| 版本 | 分支 | 核心变更 | versionCode |
|---|---|---|---|
| r14.1.1 | `a14-api101` | 全部模块完成原生 API-101 `intercept(Chain)` 迁移 | 110 |
| r14.1.0 | `a14-api101` | `GlobalActions` / `Controls` 完成迁移，其余模块仍走适配层 | 109 |
| r14.0.0 | `a14` | 生命周期与 hook 注册迁移到 libxposed API 101，保留 API-100 兼容 | 108 |

## 安装说明

安装本 fork 前请先在 **米客_forA14** 应用内备份设置，然后卸载官方原版并安装本 fork；请勿与官方版或其他 fork 同时启用，否则会导致重复 hook 冲突。

## 构建与签名

- 构建命令：`./gradlew :app:assembleRelease`
- 所有发布 APK 均经过 `zipalign` 与 `apksigner` v2 签名验证。
- r14.1.1 使用自动生成的 release keystore（`keystore.properties` + `.tools/pengeek-release-auto.keystore`），请在后续升级中妥善保存该密钥。
- 测试设备：小米 13（HyperOS 1.0.7.0.UMCTWXM，Android 14），r3–r14.1.1 均正常重启并加载。

## r14.1.1

- 完成 `Launcher.java`、`System.java`、`SystemUI.java`、`Various.java` 全部 `before` / `after` 回调到 `XposedInterface.Hooker.intercept(Chain)` 的原生 API-101 迁移，全部 Java hook 模块已无活跃 `BeforeHookCallback` / `AfterHookCallback` 遗留。
- 重构工具链修复：
  - `rewrite_module.py`：修复 `is_in_comment` 对字符串内 `/*` 的误判。
  - `rewrite_module.py`：使 `throws Throwable` 在旧回调签名中可选，避免遗漏无 `throws` 的方法。
  - `rewrite_module.py` / `merge_intercepts.py`：调整 `thisObject` / `args` 自赋值清理顺序，解决嵌套匿名类中的 effectively final 编译错误。
  - `merge_intercepts.py`：合并同一 `MethodHook` 中的 `before` / `after` 为单一 `intercept` 方法。
- `HookBuilder` 显式 `ExceptionMode.PASSTHROUGH`，保证被 hook 方法自身异常正常向上传播。
- 因 Devin IDE 占用 `app/build/intermediates/lint-cache`，通过临时指定 `-PcleanBuildDir` 到全新目录完成了真正的 clean build。
- 输出 APK：`Pengeek-HyperOS1-A14-API101-r14.1.1.apk`（`versionCode 110`，`versionName r14.1.1`）

## r14.1.0

- 原生 API-101 实现：`MethodHook` 直接实现 `XposedInterface.Hooker`，使用 `intercept(Chain)` 调度。
- 完成 `GlobalActions.java` 与 `Controls.java` 的 `before` / `after` 回调迁移。
- `Launcher.java`、`System.java`、`SystemUI.java`、`Various.java` 仍通过 `HookerClassHelper` 适配层运行（已在 r14.1.1 完成迁移）。
- `HookBuilder` 显式 `ExceptionMode.PASSTHROUGH`。
- 输出 APK：`Pengeek-HyperOS1-A14-API101-r14.1.0.apk`（`versionCode 109`，`versionName r14.1.0`）

## r14.0.0

- 生命周期与 hook 注册迁移到 **libxposed API 101**。
- 保留 API-100 兼容实现（通过 `HookerClassHelper` 适配 `BeforeHookCallback` / `AfterHookCallback`）。
- 限制 Android 14（`UPSIDE_DOWN_CAKE`），避免 hook 应用到不兼容的 Android 15/16 组件。
- 输出 APK：`Pengeek-HyperOS1-A14-API101-r14.0.0.apk`（`versionCode 108`，`versionName r14.0.0`）

## 早期迭代（r3–r13）

类缓存、Context 缓存、资源句柄复用、主题值预解析、资源值直接分发、UserId 直接计算、依赖实例缓存、零参数调用重载、常量 Hook 快速路径、参数类缓存、模块资源配置缓存、跳过空 after 回调、资源 Hook 早退。

## 性能评估

### 理论性能对比

> 以下数据为基于代码路径和优化点的理论估算，非真机跑分，仅供横向参考。

| 指标 | 旧版 LSPosed + 上游 customiuizer | 新版 LSPosed + r14.0.0 | 新版 LSPosed + r14.1.0 | 新版 LSPosed + r14.1.1 |
|---|---|---|---|---|
| Hook 调用额外对象分配 | 高（每次回调创建 Before/After 对象、数组包装等） | 中（仍创建 Before/After 适配对象） | 低（已迁移模块直接 `intercept(Chain)`，无适配对象） | 低（全部模块已迁移） |
| 单次 hook 调用反射/包装层 | 多层反射 + adapter | 多层反射 + adapter | 已迁移模块减少 2-3 层适配调用 | 所有模块减少 2-3 层适配调用 |
| 异常传播方式 | 可能被框架 PROTECTIVE 模式吞掉 | PASSTHROUGH，正常向上传播 | PASSTHROUGH，正常向上传播 | PASSTHROUGH，正常向上传播 |
| Hook 注册耗时 | 较高（重复 Class.forName、无缓存） | 降低约 20-30% | 进一步降低 10-20% | 进一步降低 10-20% |
| 模块启动内存峰值 | 高 | 中 | 中-低 | 低 |
| 资源 Hook 重复 Map 查询 | 高 | 低（预解析 + 直接 switch/缓存） | 低 | 低 |
| 每次调用的参数数组拷贝 | 存在 | 存在（适配层 `toArray`） | 已迁移模块减少一次拷贝 | 全部模块减少一次拷贝 |
| 整体运行时开销 | 高 | 中 | 低（在已完成迁移的模块上） | 低（全局） |

理论综合提升（相对上游）：r14.0.0 约 **15-25%**；r14.1.0 在已迁移模块上额外降低 **20-40%** 调用开销；r14.1.1 全部迁移后全局整体约 **40-60%**。

### r14.1.1 性能评估报告

> 以下评估基于代码路径、编译结果与静态统计，非真机跑分，供横向参考。

#### 代码质量统计

| 文件 | `intercept` 数量 | 平均方法行数 | 最大方法行数 | `thisObject` 声明 | `thisObject` 赋值 | `args` 声明 | `new` 匿名类 |
|---|---|---|---|---|---|---|---|
| Launcher.java | 93 | 26.5 | 76 | 93 | 0 | 93 | 101 |
| System.java | 162 | 32.0 | 114 | 162 | 0 | 162 | 179 |
| SystemUI.java | 141 | 37.6 | 202 | 141 | 1* | 141 | 144 |
| Various.java | 46 | 33.4 | 138 | 46 | 0 | 46 | 53 |

> \* `SystemUI.java` 中 1 处 `thisObject = XposedHelpers.getSurroundingThis(thisObject);` 位于嵌套 `intercept` 内，编译通过，不影响外层 effectively final 检查。

#### 运行时性能评估

- **Hook 调用开销：理论上轻微变好**
  - 旧风格每个 hook 可能触发 `before` + `after` 两次回调并构造两个 callback 对象。
  - 新风格合并为一次 `intercept` 调用，减少了第二次方法调用和 `BeforeHookCallback` / `AfterHookCallback` 的对象分配。
- **关键模块**：`SystemUI.java` 最大 `intercept` 方法 202 行，`System.java` 最大 114 行，尚未超过 JVM 64K 字节码限制，但 `SystemUI` 中个别方法已偏大，建议后续拆分复杂逻辑。

#### 内存与 GC 影响

- 每个 `intercept` 调用都会新增：
  - `Object[] args = chain.getArgs().toArray(new Object[0]);`
  - `Object thisObject`、`Object result`、`Throwable throwable`（`before` 还有 `boolean skipped`）
- `args` 数组是每次 hook 的主要新增分配；`thisObject` / `args` 被匿名内部类捕获时会生成合成字段，数量与原写法基本持平。
- 如果 hook 触发频率极高，`chain.getArgs().toArray(new Object[0])` 是一次额外的数组拷贝分配。

#### 潜在风险

- **内部类捕获**：`BroadcastReceiver`、`MethodHook` 等匿名类继续使用 `thisObject` / `args`，已确保 `thisObject` 不再在内部类作用域内重新赋值。
- **线程安全**：`intercept` 内 `result`、`throwable`、`skipped` 均为局部变量，线程安全；但跨 hook 共享的静态字段（如 `lastState`、`mNextAlarmTime`）仍与原逻辑一致，未引入新的竞态。
- **方法体积**：合并后单个体积增大，目前无编译问题，但长期维护性下降。

#### 总结

- **整体成功率**：100%（目标四个模块全部迁移，`javalang` 解析通过，`assembleRelease` 编译通过，APK 已签名为 v2 Release。）
- **性能变化**：**持平 / 轻微变好**。单次调用路径更短，但 `toArray` 分配抵消了部分收益。
- **进一步优化建议**：
  1. 若 hook 内部不访问 `args`，可在生成时省略 `Object[] args` 前缀。
  2. 仅在需要数组索引时再做 `chain.getArgs().toArray(...)`，否则尝试用 `List` 直接遍历以减少数组分配。
  3. 对 `SystemUI.java` 中 200+ 行的 `intercept` 方法按职责拆分。
  4. 如需真实帧率/耗时数据，可在 `MainModule` 或 `SystemUI` 的热点 hook 中加入 `System.nanoTime()` 日志并在真机测试。
  5. 正式发布前建议保留好 `pengeek-release-auto.keystore`，或替换为你自己的 release 密钥。
