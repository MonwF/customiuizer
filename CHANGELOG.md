# 更新日志

> 本项目是基于 [MonwF/customiuizer](https://github.com/MonwF/customiuizer) 的**非官方优化 fork**，沿用 **GPL-3.0** 协议。  
> 原始模块作者为 `MonwF`，上游源码由 `Mikanoshi`（CustoMIUIzer）提供。  
> 仅兼容：**HyperOS 1 / Android 14（API 34）/ libxposed API 101**。

## 基础版本

- 从 `MonwF/customiuizer` 的 `a14` 分支 fork（`open source for a14`）。
- 将生命周期与 hook 注册迁移到 **libxposed API 101**。
- 保留 API-100 风格的回调行为（可变参数、提前返回/抛出、结果替换与异常恢复）。
- 初始化限制为 Android 14（`UPSIDE_DOWN_CAKE`），避免将 HyperOS 1 的 hook 应用到不兼容的 Android 15/16 组件。

## r3 — Class 缓存

- 在 `XposedHelpers` 增加类级缓存，减少重复的 `Class.forName` 查找。

## r4 — Context 缓存 + 资源 Map 查询削减

- 缓存 `ModuleHelper.findContext()` 结果。
- 减少 `ResourceHooks.getResourceReplacement()` 中重复的 map 查询。

## r5 — 资源句柄复用

- 单次资源替换调用中缓存模块 `Resources`，并向下传给 `getFakeResource` / `getResourceReplacement`。

## r6 — 主题值预解析

- 扩展 `ResourceHooks.ThemeValue` 的预解析字段（`pkg`、`name`、`themeValueType`、`resourceType`），使 `setThemeValueReplacement` 与 `initThemeHook` 不再重复拆分字符串。

## r7 — 资源值直接分发

- 在 `ResourceHooks.getModuleResValue()` 中用直接 `switch` 替换基于反射的资源取值。

## r8 — 直接计算 UserId + Dependency 实例缓存

- 用直接算术 `Process.myUid() / 100000` 替换 `UserHandle.getUserId(Process.myUid())` 的反射调用。
- 在 `ModuleHelper.getDepInstance()` 中使用 `ConcurrentHashMap` 实例缓存，并缓存 `Dependency.get` 的 `Method`。

## r9 — 零参数调用重载

- 为 `XposedHelpers.callMethod`、`callStaticMethod`、`newInstance`、`findMethodBestMatch`、`findConstructorBestMatch` 增加零参数重载。
- 共享 `EMPTY_OBJECT_ARRAY` / `EMPTY_CLASS_ARRAY` 单例，并在 `HookerClassHelper.BeforeHookCallback` 中复用空 `Object[]`。

## r10 — 常量 Hook 快速路径

- 为 `DO_NOTHING` / `returnConstant` 回调增加 `ConstantHooker`，直接返回值，跳过 `BeforeHookCallback` / `AfterHookCallback` 的创建。

## r11 — 参数类缓存

- 以 `(ClassLoader, 参数签名)` 为 key 缓存 `XposedHelpers.getParameterClasses()` 解析后的 `Class<?>[]`，减少 hook 注册阶段重复的 `findClass` 工作。

## r12 — 模块资源配置缓存

- 在 `ModuleHelper.getModuleRes()` 中缓存模块 `Resources`，设备 `Configuration` 未变化时直接复用；配置变化后自动重建。

## r13 — 跳过空的 after 回调

- `CustomHooker` 检测 `MethodHook` 是否覆写了 `after()`；未覆写时跳过 `AfterHookCallback` 的创建与调用。

## r14 — 资源 Hook 早退

- `ResourceHooks.mReplaceHook.before` 在获取模块上下文与资源前先检查 `fakes` / `resourceIdReplacements`。
- `OBJECT` 类型替换直接返回对象，不再触碰模块资源。
- 复用 `param.getArgs()[0]` 中已有的 `Integer` 对象作为 `ConcurrentHashMap` key，避免额外装箱。

## r14.0.0

- 确定版本规划：
  - **r14.0.\*** 继续维护 API-100 风格回调（当前版本）；
  - **r14.1.\*** 将代码重写为原生 API-101 风格。
- 修正安装说明：安装本 fork 前请先在**米客（Pengeek）应用内**备份设置，然后卸载官方原版并安装本 fork；两个模块不能同时启用，否则会导致冲突。
- 模块名称统一为 **米客(r14) / Pengeek(r14)**，模块简介标注为“仅适用于 A14 的 HyperOS 1”。
- `versionCode`：`108`
- `versionName`：`r14.0.0`
- 输出 APK：`Pengeek-HyperOS1-A14-API101-r14.0.0.apk`

## 构建 / 校验

### r14.0.0 测试版

- APK：`Pengeek-HyperOS1-A14-API101-r14.0.0.apk`
- `versionCode`：`108`
- `versionName`：`r14.0.0`
- 使用 debug 密钥库签名，仅用于本地/测试。
- 已通过 `zipalign` 与 `apksigner`（v3 签名）验证。

### r14.0.0 正式版（release-signed）

- APK：`Pengeek-HyperOS1-A14-API101-r14.0.0.apk`
- 包名：`name.monwf.customiuizer.r14`（已改为与官方 MonwF 版不同，避免签名/包名冲突）
- 应用名：`Pengeek(r14)` / `米客(r14)`
- **安装本 fork 前请先在米客（Pengeek）应用内备份设置，然后卸载官方原版并安装本 fork。请勿与官方版或其他分支同时启用，否则会出现重复 hook 冲突。**
- `versionCode`：`108`
- `versionName`：`r14.0.0`
- 使用 release 密钥库（`pengeek-release.keystore`）签名，v3 签名。
- 已通过 `zipalign` 与 `apksigner` 验证。

## 测试设备

| 设备 | HyperOS | Android | SoC | 内存 | 说明 |
|------|---------|---------|-----|------|------|
| 小米 13 (2211133G) | 1.0.7.0.UMCTWXM | 14 (UKQ1.230804.001) | Snapdragon 8 Gen 2（最高 3.19 GHz）| 12 GB | 主要测试机；r3–r14 均正常重启并加载。 |

- 基带版本：`MPSS.DE.3.0.c1-GLB-Oct 17 2024-04:43:46`
- 内核版本：`5.15.123-android13-8-00008-g3ca6a2912c7e-ab11087001`
