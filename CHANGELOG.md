# 更新日志

本文件只记录 **CustoMIUIzer A14** 的独立发布线。项目以 [MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) 为 Android 14 功能参考，后续版本围绕两条主线演进：

- 将 Hook 基础设施迁移并稳定在 **libxposed API 101**。
- 在不扩大兼容风险的前提下优化代码、线程、缓存、资源与异常边界。

性能结论会区分静态分析与实机验证；未做同设备功耗采样时，不使用推测性续航或速度百分比。

## r14.6.3

发布日期：2026-07-25。状态：**开发版（生命周期治理与 ClassNotFound 回退适配）**。

### 修复与改进

- **ClassNotFound 异常回退**：`XposedHelpers.findClassInternal` 在类加载器无法找到类时，增加对 `Application` 层 ClassLoader 的回退查找，减少 systemui/keyguard 内部类加载失败。
- **Owner 级 PreferenceObserver 管理**：`ModuleHelper` 新增 `observePreferenceChange(PreferenceObserver, Object)` 与 `removePreferenceObserver(Object)`，支持以生命周期宿主为 owner 注册/注销偏好监听，避免静态观察者重复注册与内存泄漏。
- **Controls.java 长按回调治理**：`NavBarActionsHook` 在 `postDelayed` 之前先 `removeCallbacks`，防止导航键长按 Runnable 重复堆积。
- **Launcher.java 生命周期治理**：
  - `RenameShortcutsHook` 使用 owner 方式注册 `PreferenceObserver`，并在 `onDestroy` 中移除。
  - `PrivacyFolderHook` 与 `setupLauncher` 的 `BroadcastReceiver` 改为具名变量、先注销旧实例再注册，并在 `onDestroy` 中统一清理。
- **System.java 生命周期治理**：
  - 所有 `observePreferenceChange` 调用改为以 `thisObject` 为 owner 注册。
  - `KeyguardSecurityContainerController`、`KeyguardViewMediator`、`BluetoothControllerImpl`、`MiuiPhoneStatusBarPolicy` 等处的匿名 `BroadcastReceiver` 先注销旧实例再注册，避免重复。
  - `HeadsUpManager` 的 `mRemoveAlertRunnable`、`MultiWindowPlusHook`、`ExpandHeadsUpHook` 的 `postDelayed` 在发送前移除旧回调或复用 stored runnable，防止重复与泄漏。
- **SystemUI.java 方法拆分**：将 `MonitorDeviceInfoHook` 与 `AddCustomTileHook` 提取到 `SystemUIMonitorAndTileHooks.java`，`SystemUI.java` 保留委托 stub；通过包级可见的 helper/field 共享解决跨类依赖，降低主文件行数与维护耦合。
- **代码清理与质量修复**：清理改动文件中的未使用 import、注释死代码；修复 `SystemUIMonitorAndTileHooks` 的 `DefaultLocale` 与 `DiscouragedApi` lint 警告。
- **单元测试补齐**：新增 `XposedHelpersCacheTest`、`AppHelperTest`（与已有 `PrefMapTest` 一起），覆盖 `XposedHelpers` 反射缓存、`additional instance field` 以及 `AppHelper` 的 preferences 工具方法。
- **双排信号栏 SIM1 信号为空修复**：`SystemUI.DualRowSignalHook` 通过 `Resources.getResourceName` 动态解析 `stat_sys_signal_*` 资源名中的信号等级，并缓存结果；修复 MIUI/HyperOS 5G/4G 后缀（如 `stat_sys_signal_3_5g`）导致映射失败、SIM1 显示为空的问题。

### 构建产物

- `versionCode` 163 / `versionName` r14.6.3。

## r14.6.2

发布日期：2026-07-24。状态：**稳定版（r14.6.x 终版；合并 r14.6.0/14.6.1 历史、清理死代码与发布标签；无业务逻辑改动）**。

### r14.6.x 演进

- **r14.6.0 原始尝试**：
  - D 类废弃 API/权限迁移：将 `Settings.System` 自定义键（`systemui_restart_time`、`last_music_paused_time`、`dark_mode_enable_by_setting`）迁移到模块 `SharedPreferences`；新增 `Helpers.getSystemSharedPrefs()`。
  - PendingIntent flag 兼容辅助：在 `Helpers` 中新增 `getMutableActivityPendingIntent` / `getImmutableActivityPendingIntent`。
  - 通知渠道：`MainApplication.onCreate` 创建默认低重要性 `NotificationChannel`（ID `customiuizer_default`）。
  - 权限声明：`AndroidManifest.xml` 新增 `WAKE_LOCK` 与 `POST_NOTIFICATIONS`。
- **r14.6.1 热修复**：
  - 回退 `Settings.System` 自定义键迁移，恢复使用 `Settings.System`；移除 `Helpers.getSystemSharedPrefs()`，避免 `onPackageReady` 早期 `Context` 尚未绑定到应用数据目录时调用 `getSharedPreferences` 触发 `RuntimeException: No data directory found for package android`。
  - 保留通知渠道、`WAKE_LOCK`/`POST_NOTIFICATIONS` 权限声明、PendingIntent flag 兼容辅助。
  - LSPosed 日志（`LSPosed_2026-07-24T23_06_27.328222`）确认无模块相关报错。
- **r14.6.2 清理与发布**：
  - 删除 GitHub `r14.6.0` 和 `r14.6.1` release 与 tag（两个版本均已废弃或合并）。
  - 合并 r14.6.0/14.6.1 变更记录到本版本。
  - 移除 `Helpers.java` 中未使用的 `android.content.SharedPreferences` 导入。
  - 移除 `AndroidManifest.xml` 中未使用的 `POST_NOTIFICATIONS` 权限声明；`WAKE_LOCK` 仍保留（`Controls.java` 实际使用 `WakeLock`）。
  - 重写 `README.md`，新增“各版本全方位对比”表。

### 构建产物验证

- `versionCode` 162 / `versionName` r14.6.2。
- APK：`CustoMIUIzer-A14-r14.6.2.apk`，2,900,213 bytes，SHA-256: `065C43CD00A199A8363D1AD0D6F296270C32A645230113EC4DFF5AF5754ECA18`。
- 通过 `gradlew assembleRelease`；`lintVitalRelease` 0 错误，既有 ROM API 兼容性警告保持。
- 签名证书与 r14.5.0 一致；包名不变，可覆盖安装。

### 实机验证

- LSPosed 重启日志（`LSPosed_2026-07-25T00_44_12.472641`）未出现模块相关 `AndroidRuntime`/`FATAL`/`am_crash`/`am_anr`；`tv.withaibuild.customiuizer.r14` 包正常安装，系统服务与 KernelSU 策略加载无异常。
- 日志中存在的 `OnGlobalListenerError`、`ImageView`、`TransitionCallback`、`WindowElement` 等系统/其他模块 tag 异常均不含本模块调用栈，未追溯到 CustoMIUIzer 代码。
- 本次日志 `VectorModuleManager` 未打印新的模块加载样本；r14.5.0+ 同包名阶段历史 3 个样本加载成功，中位数 57 ms。
- 建议后续再抓一次开启全部作用域后的启动日志，以补全 r14.6.2 的加载数据。

## r14.5.0

发布日期：2026-07-24。状态：**稳定版，构建产物通过 `assembleRelease`，完整重启后日志中无模块相关 `AndroidRuntime`/`FATAL`/`am_crash`/`am_anr`**。

### 包名与工程结构迁移

- Java 源码目录从 `name/monwf/customiuizer` 整体迁移到 `tv/withaibuild/customiuizer`。
- 所有 Java `package`/`import`、XML 组件 `android:name`、Preference 屏幕 `class`、Shortcuts `targetClass`/`targetPackage`、Tasker 组件、`proguard-rules.pro` 的 keep 规则统一替换为 `tv.withaibuild.customiuizer`。
- `app/build.gradle`：`namespace` 与 `applicationId` 同步改为 `tv.withaibuild.customiuizer` / `tv.withaibuild.customiuizer.r14`；`versionCode` 150 / `versionName` r14.5.0。
- `META-INF/xposed/java_init.list` 入口类改为 `tv.withaibuild.customiuizer.MainModule`。

### A 类静态清理

- `AboutFragment`、`System.java`、`SystemUI.java`、`SeekBarPreference`、`ColorSelector` 中的 `String.format(Locale.getDefault(), ...)` / 无 Locale `String.format(...)` 统一指定 `Locale.US`，避免默认 locale 影响数值格式。
- `AppDataAdapter`、`LockedAppAdapter`、`ModSearchAdapter`、`PrivacyAppAdapter`、`ResolveInfoAdapter`、`SortableList` 的 `String.toLowerCase()` 统一指定 `Locale.ROOT`，保证过滤比较不受 locale 大小写规则影响。
- `MainActivity` 重置设置弹窗中的 `SharedPreferences.commit()` 改为 `apply()`，避免阻塞 UI 线程。
- `GlobalActions`、`Various`、`BTList`、`WiFiList` 的 `new Handler()` 改为 `new Handler(Looper.getMainLooper())`，防止在无线程 Looper 的 Hook/后台线程上创建 Handler。

### B 类生命周期加固

- `Controls`、`GlobalActions`、`Launcher`、`System`、`Various`、`WiFiList` 中缺少导出标志的 `registerReceiver(..., IntentFilter)` 调用统一补全 `Context.RECEIVER_EXPORTED` / `Context.RECEIVER_NOT_EXPORTED`。
- 系统广播（`SCREEN_ON`、`TIME_SET`、`TIME_TICK`、`TIMEZONE_CHANGED`、`LOCALE_CHANGED`、`WifiManager`、`BATTERY_CHANGED` sticky）使用 `RECEIVER_NOT_EXPORTED`。
- 模块自定义动作广播（`GlobalActions` 自定义 `ACTION_PREFIX` 事件、`FETCHAPPCONFIG`、`PUSHAPPCONFIG`）使用 `RECEIVER_EXPORTED`。
- 不影响已带 `RECEIVER_*` 标志的现有注册点。

### C 类性能缓存

- `Helpers.java` 新增线程安全的 `getResId(Resources, name, defType, defPackage)` 缓存，使用 `ConcurrentHashMap` 缓存 `Resources.getIdentifier()` 结果。
- `System.java` 与 `SystemUI.java` 中高频 `getResources().getIdentifier(...)` / `res.getIdentifier(...)` 调用全部收敛到 `Helpers.getResId()`，减少 SystemUI/设置进程初始化与状态栏刷新热路径上的重复资源 ID 查找。
- 缓存键包含包名、资源类型与资源名，命中时直接返回缓存整型 ID；参数任一为空时返回 0，避免无效调用。

### 构建产物验证

- `versionCode` 150 / `versionName` r14.5.0。
- APK：`CustoMIUIzer-A14-r14.5.0.apk`，2,886,373 bytes，SHA-256: `DCB9EBC4BBE7AEE721B58F83B5371E1030AD7CAB0C4FE6CC4EAD900C420E8C93`。
- 通过 `gradlew assembleRelease`；`lintVitalRelease` 0 错误，既有 ROM API 兼容性警告保持。
- 签名证书与 r14.3.1 一致，包名不同不能覆盖安装旧包名版本，需先卸载旧模块并重新启用作用域。

### 实机验证

- `LSPosed_2026-07-24T21_24_25.003081` 完整重启后，`VectorModuleService` 已向 `tv.withaibuild.customiuizer.r14` 发送模块 binder，日志中无模块相关 `AndroidRuntime`/`FATAL`/`am_crash`/`am_anr`。
- SystemUI、Launcher、Settings 等目标进程无 CustoMIUIzer 引发的崩溃、ANR 或异常栈。

## r14.3.1

发布日期：2026-07-24。状态：**稳定版，构建产物通过 `assembleRelease`，可直接覆盖 r14.2.9 及更早版本升级**。

### 锁屏充电数据

- `System.ChargingInfoHook` 新增 `isKeyguardIndicationCaller()` 栈追踪判断，仅对 `KeyguardIndicationController` 调用路径修改 `ChargeUtils.getChargingHintText` 返回结果，避免充电动画/充电视图等其他调用方显示同一行数据。
- 增加 `isChargingInfoHooked` 静态防护，防止同一进程内重复注册 `getChargingHintText` Hook。
- 在拼接信息前检查 `hint.contains(info)`，若原始提示文本已包含相同数据则不再追加，杜绝同一字符串内出现两行重复充电数据。

### 代码清理与依赖优化

- `PreferenceFragmentBase`: `SimpleDateFormat` 指定 `Locale.US`，避免依赖默认 locale。
- `AppDataAdapter`: `String.toLowerCase()` 指定 `Locale.ROOT`。
- `Various`: 广播接收器中的 `SharedPreferences.commit()` 改为 `apply()`。
- `BatteryIndicator`: 缓存 `status_bar_height` 资源 ID，减少重复 `getIdentifier` 调用。
- `build.gradle`: 升级 `com.github.ben-manes.versions` 插件至 `0.54.0`。
- `app/build.gradle`: 升级 `kotlin-bom` 至 `2.2.21`。

### 减少无效 Hook 与资源替换

- 新增 `SystemUI.hasStatusBarModifications()`，汇总 `setupStatusBar` 中所有资源替换与状态栏文本图标开关条件，包括状态栏边距、控制中心样式、音量计时器、图标大小、步数显示、抽屉日期、点击解锁、锁屏超时、电池/设备温度等。
- `MainModule` 在 `SystemUIInitializer.init` 中仍注册初始化 Hook，但 `SystemUI.setupStatusBar` 内部先判断 `hasStatusBarModifications()`；无任何相关功能开启时，仅写入 `systemui_restart_time` 标记，不调用 `addFakeResource` 和 `setThemeValueReplacement`/`setResReplacement`。
- 避免 SystemUI 每次启动时加载 `statusbar_text_icon` 假资源，以及反复替换 `status_bar_padding_top` 等默认资源，降低功能关闭时的内存与跨进程写入开销。

### 统一线程与生命周期治理

- `WeatherDataController` 不再在每次 `TIME_TICK` 触发时 `new Thread()` 查询天气，改为进程内单一 `ExecutorService` 队列后台查询，减少每分钟新建线程与栈内存抖动。
- `WeatherDataController` 的 `Handler` 改为静态 `Looper.getMainLooper()` 实例，避免 `forceRefresh` 分支重复创建 `new Handler`。
- `WeatherDataController` 在 `initContext` 重复进入时先 `unregisterReceiver` 旧 `TIME_TICK` 接收者，再注册新接收者，并显式指定 `Context.RECEIVER_NOT_EXPORTED`。

### 构建产物验证

- `versionCode` 130 / `versionName` r14.3.1。
- APK：`CustoMIUIzer-A14-r14.3.1.apk`，2,886,165 bytes，SHA-256: `E1ED1FEF9108E9A94D1B532F5B3BCDBD71AF5DC32E610A239CF108A9ABEC57D8`。
- 通过 `gradlew assembleRelease`；`lintRelease` 0 错误，既有 ROM API 兼容性警告保持。
- `libxposed` 保持 API 101，未升级到 102，避免运行时兼容风险。
- 签名证书与 r14.2.9 一致，可直接覆盖升级。

### 实机验证

- `LSPosed_2026-07-24T13_52_32.282030` 完整重启后，`VectorModuleManager` 两次加载 `tv.withaibuild.customiuizer.r14` 分别为 6 ms 与 24 ms，日志中无模块相关 `AndroidRuntime`/`FATAL`/`am_crash`/`am_anr`。
- SystemUI、Launcher、Settings 等目标进程无 CustoMIUIzer 引发的崩溃、ANR 或异常栈。

## r14.2.9 — 生命周期与绘制热路径（合并 r14.2.8-r14.2.9）

发布日期：2026-07-23。状态：**稳定版，LSPosed 日志（`LSPosed_2026-07-23T21_52_34.250866`）确认无 CustoMIUIzer 相关崩溃或异常**。

### 生命周期与线程调度

- `StepCounterController.initContext` 改为静态持有 `TIME_TICK` 广播接收者，重复进入（如 SystemUI 重建）时先注销旧接收者再注册新接收者，防止接收者泄漏与重复查询步数。
- `StepCounterController` 的 `Handler` 改用 `mContext.getMainLooper()`，避免在无线程 Looper 的 Hook 线程上创建 `Handler` 失败。
- `StepCounterController.removeStepViewByTag` 改用 `ArrayList.removeIf()`，消除 `for` 循环中 `remove()` 可能触发的并发修改异常。

### 绘制热路径

- `BatteryIndicator.updateDisplaySize` 缓存 `mDensity` 与 `mStatusBarHeight`，`updateDrawable` 不再每次绘制时查询 `Resources.getDisplayMetrics()` 和解析 `status_bar_height` 资源 ID。
- `BatteryIndicator.updateParameters` 移除每次调用时新建的 `Matrix` 对象，改为 `setImageMatrix(null)` 复用默认矩阵。

### 构建产物验证

- `versionCode` 127 / `versionName` r14.2.9。
- APK：`CustoMIUIzer-A14-r14.2.9.apk`，2,886,165 B。
- SHA-256：`dabc71b2e5b5353f03ddf2ba513567888b6f87fc8f71994c3122dc3304cf6e10`。
- 通过 `gradlew test` 与 `gradlew assembleRelease`；3 项单元测试通过，`lintRelease` 0 错误、既有 ROM API 兼容性警告保持。
- 签名证书与 r14.2.7 一致，可直接覆盖升级。

### r14.2.8 累积变更

### 热路径与偏好监听

- `MainModule` 偏好变化监听不再每次调用 `sharedPreferences.getAll()` 复制整个远程偏好表，改为根据 `PrefMap` 中已有值类型直接调用 `getBoolean`/`getInt`/`getString`/`getLong`/`getFloat`/`getStringSet` 单次读取；未命中或新键时回退到 `getAll()`，显著降低设置项调整时的内存与 CPU 开销。
- `GlobalActions.setupStatusBar` 内的 `MiuiFreeformModeController`（PinningWindow，动作码 28）、`SoScSplitScreenController`（SplitScreen，动作码 29）与 `AutoBrightnessController`（ToggleAutoBrightness，toggle 6）Hook 不再在任意自定义动作开启时全部注册，仅在对应动作被配置时才注册，进一步减少 `com.android.systemui` 中的无效 Hook 与 BroadcastReceiver。

### 无效/失效代码清理

- 移除 `GlobalActions.mSBReceiver` `OpenVolumeDialog` 分支内被错误嵌套、永远不会执行的 `ToggleZenMode` / `ToggleNightMode` 子分支；这两个动作已由顶层分支正确处理。

## r14.2.7 — 功能关闭零成本与生命周期治理

发布日期：2026-07-23。状态：**稳定版，LSPosed 日志确认无 CustoMIUIzer 相关崩溃或异常**。（r14.2.5 与 r14.2.6 因状态栏过渡异常已回退并删除 tag/release，r14.2.7 为 r14.2.4 之后的下一个稳定版本）

### 按开关减少无效 Hook 与监听

- `GlobalActions.hasCustomActions()` 集中判断是否存在任何自定义动作；未配置任何自定义动作时，`MainModule` 不再调用 `GlobalActions.setupGlobalActions` 和 `GlobalActions.setupStatusBar`，避免在 `system_server` 和 `com.android.systemui` 中注册 `mSBReceiver`、自由窗口/分屏/自动亮度控制器的 Hook。
- `MainModule` 仅在 `launcher_privacyapps_gest` 开启时调用 `Launcher.setupLauncher`，避免为所有桌面进程注册 `FETCHAPPCONFIG` / `PUSHAPPCONFIG` 广播接收者。

### 生命周期与异常边界

- `SystemUI` 手电筒状态 `ContentObserver` 与 `Various` 下一闹钟 `ContentObserver` 改为 `new Handler(mContext.getMainLooper())`，不再依赖当前线程 Looper，防止在后台线程创建时崩溃。
- `System.java` 秒级时钟刷新 `TIME_SET` 广播接收者改为用 `AdditionalInstanceField` 保存旧实例并先注销再注册，且仅在真正启用秒针显示时才注册，避免 `MiuiStatusBarClockController` 重建时累积多个接收者。

### 构建产物验证

- `versionCode` 125 / `versionName` r14.2.7。
- APK：`CustoMIUIzer-A14-r14.2.7.apk`，2,886,165 B。
- SHA-256：`75FA704A6D07880EE7BA4221A6E687FFCF9BDFCF571FFDB5E0F557713D9806FA`。
- 通过 `gradlew test` 与 `gradlew assembleRelease`；3 项单元测试通过，`lintRelease` 0 错误、既有 ROM API 兼容性警告保持。
- 签名证书与 r14.2.4 一致，可直接覆盖升级。

## r14.2.4 — 关闭时零成本与接收者防重注册（合并 r14.2.1-r14.2.4）

发布日期：2026-07-23。状态：**稳定版，LSPosed 日志确认无 CustoMIUIzer 相关崩溃或异常**。

### 按开关减少无效 Hook 与监听

- `MainModule` 在 `SystemUI` 进程中通过 `SystemUI.hasControlCenterModifications()` 判断是否存在控制中心/音量相关定制；未开启任何对应功能时不再注册 `ControlCenterPluginHook`，避免加载 `miui.systemui.plugin` 的插件 loader 及后续大量 UI 回调。
- `GlobalActions` 中 `MiuiFreeformModeController`、`SoScSplitScreenController` 与 `AutoBrightnessController` 的 `BroadcastReceiver` 注册前，先通过 `AdditionalInstanceField` 检查并注销旧接收者，再写入新实例；重复进入 `onInit`/`构造` 时不会累积多个接收者。
- 保留 r14.2.3 的 `ContentObserver` / `BroadcastReceiver` 生命周期治理、r14.2.2 的反射缓存哨兵与 r14.2.1 的热路径缓存边界，不扩大未验证的 API 101 原生拦截范围。

### 构建产物验证

- `versionCode` 122 / `versionName` r14.2.4。
- APK：`CustoMIUIzer-A14-r14.2.4.apk`，2,886,165 B。
- SHA-256：`24A22518B29F9714012E01A1B81BEA4905B58492FE31478F7D5A8228BA9EABE6`。
- 通过 `gradlew test` 与 `gradlew assembleRelease`；3 项单元测试通过，`lintRelease` 0 错误、既有 ROM API 兼容性警告保持。
- 签名证书与 r14.2.0 一致，可直接覆盖升级。

### r14.2.1 累积变更

### 偏好读取与反射缓存

- `PrefMap.getStringAsInt` 增加解析后整数值缓存，命中时直接返回并在 `put`/`remove` 时失效；避免高频 Hook 读取同一字符串键时重复 `Integer.parseInt`。
- `XposedHelpers` 的 `findField` / `findMethodExact` / `findMethodBestMatch` / `findConstructorExact` / `findConstructorBestMatch` 缓存从 `computeIfAbsent` 改为显式 `get`/`put`，消除每次缓存查找时 lambda 与 `Optional` 捕获对象的分配。
- `ResourceHooks.mReplaceHook` 不再调用 `XposedHelpers.getArgsArray(args)` 构造完整 `Object[]`，直接按 `List<Object>` 下标读取所需参数，减少资源重定向路径上的短命数组。
- `ModuleHelper.getDepInstance` 缓存键由 `className + "@" + identityHashCode(classLoader)` 改为解析后的 `Class<?>`，避免字符串拼接与哈希计算。

### 绘制与颜色计算

- `BatteryIndicator` 复用 `ArgbEvaluator`、`mRainbowColors` / `mRainbowPositions` 数组、`RoundRectShape` 与 `ShapeDrawable.ShaderFactory` 实例；彩虹模式不再每次重绘时创建 `float[]` 与匿名 `ShaderFactory`。
- `AudioVisualizer` 复用单个 `float[3]` 计算 `Color.HSVToColor`，并避免 `DashPathEffect` 的临时 `float[]`；减少随机颜色与彩虹色条更新时的分配。

### r14.2.2 累积变更

### 反射缓存

- `XposedHelpers` 的 `fieldCache` / `methodCache` / `constructorCache` / `classCache` 将值类型由 `Optional<T>` 改为 `Object`，使用单例 `NOT_FOUND` 哨兵对象表示“未找到”，避免每次缓存命中或写回时创建 `Optional` 实例与 `orElseThrow` lambda 捕获对象。
- `findClass` / `findClassIfExists`、`findField` / `findFieldIfExists`、`findMethodExact` / `findMethodBestMatch`、`findConstructorExact` / `findConstructorBestMatch` 保持显式 `get`/`put` 流程；未命中时存入哨兵，`findClassIfExists` / `find*IfExists` 仍返回 `null`。

### r14.2.3 累积变更

### 监听与调度

- `SystemUI` 自定义磁贴（`custom_5G`、`custom_floatingtime`）和锁屏手电筒入口的 `ContentObserver` 在重复监听/Hook 触发时先注销旧观察者并清理字段，避免同一实例多次注册导致通知重复触发和泄漏。
- `Various` 的 `AlarmCompatServiceHook` 为 `next_alarm_clock_formatted` 观察者增加防重复注册，重复进入 `onBootPhase` 时不会留下多个 ContentObserver。
- `BatteryIndicator` 的偏好监听不再每次 `onChange` 创建 `Handler(Looper.getMainLooper())`，改用 `View.post()`；测试动画结束后的延迟 `Runnable` 也复用单一实例，减少短生命周期对象。
- `WeatherDataController` 的 `TIME_TICK` 广播接收者与延迟刷新 `Handler` 改为进程单例并只在首次 `initContext` 注册，避免时钟控制器重建时重复注册接收者和 Handler 泄漏。

## r14.2.0 — Hook 热路径与调度架构优化

发布日期：2026-07-23。状态：**稳定版，已完成目标设备安装、完整重启、功能与日志验证**。

### 高频偏好读取

- 将远程偏好的 `pref_key_` 前缀从“每次读取时拼接”改为“载入或更新时一次规范化”。源码静态扫描覆盖 476 个 Hook 偏好读取点；常规读取不再创建前缀字符串。
- 每次读取由最多两次 `HashMap.get()` 收敛为一次取值，并以 `ConcurrentHashMap` 保证偏好监听线程与 UI、Binder、system_server 回调之间的可见性。
- 保留对原始 `pref_key_*` 动态键的兼容读取，Launcher 应用重命名和偏好观察者无需改变协议。
- 缺失的字符串集合复用不可变空集合，不再为每次默认读取创建 `HashSet`。

### API 101 Hook 基础设施

- 兼容层的参数数组改为按需生成：只有回调实际调用 `getArgs()` 时才把 API 101 参数列表转换为数组；不读取参数的 before/after Hook 直接使用 `chain.proceed()`。
- 清理原生 `intercept(Chain)` 生成代码中 101 个从未读取的参数数组；另有 63 处参数只被原样传回，改用无复制 `chain.proceed()`。合计移除 164 处数组转换和 116 次从未使用的 `getThisObject()`。
- 删除 85 个始终为 `false` 的模板状态变量及 105 个不可能进入的分支；真正需要提前返回的 79 个拦截器继续保留原有状态语义。
- 后置回调签名检测从 Hook 对象构造期改为兼容层首次实际执行时惰性完成。直接覆盖 `intercept` 的原生 API 101 Hook 不再承担无效反射扫描。
- 删除重复的参数签名字符串缓存。方法与构造器缓存继续保留，但注册 Hook 时不再额外构造缓存键字符串并长期保存同一组参数类型。
- 移除项目内没有调用方的方法深度计数兼容代码，减少维护面；不影响 libxposed API 101 的 Hook 深度或异常透传语义。

### 线程、调度与动态更新

- 状态栏/控制中心显示秒数时，不再创建独立 `Timer` 线程后再转发到主线程；改为主线程 `Handler` 对齐秒边界的单一循环，关闭秒数后立即移除回调。
- 偏好观察者集合改为写时复制集合，避免监听注册与偏好分发并发时的迭代失效；观察者数量小且注册低频，适合读多写少场景。
- 复用首次取得的远程偏好代理，避免初始化和注册监听时重复请求同一服务对象。
- 修复音量面板折叠/展开模糊值的观察者使用无前缀键比较，导致设置变化后不能即时更新的问题。

### 代码与语言决策

- r14.2.0 不进行 Java 到 Kotlin 的机械迁移。Kotlin 不天然比 Java 更快；当前模块的 Kotlin 标准库由 API 101/DexKit 依赖链带入，重写高频 Hook 还可能增加包装、空值检查和分配，无法形成可测量收益。
- 继续使用 Java 17 编译、R8 优化、资源压缩和 API 101 混合 Hook 边界；SystemUI 不做未经实机验证的全量原生迁移。
- 关闭与运行无关的 APK 内嵌 Git 修订元数据；版本来源由正式标签记录，使相同源码和签名环境下的 Release 产物不再因当前提交点变化而改变哈希。
- 新增 `PrefMap` 单元测试，覆盖远程键规范化、批量加载、动态删除、默认值和原始前缀兼容读取。

### 相对 r14.1.3 的静态评估

| 指标 | r14.1.3 | r14.2.0 | 结论 |
|---|---:|---:|---|
| APK 大小 | 2,886,250 B | 2,886,165 B | 减少 85 B；保留全部语言和功能资源 |
| `classes.dex` | 1,265,844 B | 1,266,104 B | 增加 260 B（0.02%），换取线程安全与惰性基础设施 |
| 无效/无改写参数数组转换 | 164 处 | 0 处 | 删除可证明不需要的短命数组 |
| 无效 `getThisObject()` | 116 处 | 0 处 | 删除可证明未使用的接口调用 |
| 恒假模板状态/分支 | 85 / 105 处 | 0 / 0 处 | 删除生成器遗留死代码 |
| 偏好读取 | 运行时拼接前缀，最多查表 2 次 | 载入时规范化，读取查表 1 次 | 降低高频回调分配与哈希工作 |
| 兼容层参数数组 | 每次回调创建 | 仅调用 `getArgs()` 时创建 | 未使用参数的 Hook 不再分配 |
| 秒钟刷新线程 | 1 个 `Timer` 线程 + 主线程转发 | 仅主线程对齐调度 | 减少线程和跨线程唤醒 |

以上为代码路径、调用点和构建产物的静态比较，不是同设备功耗跑分。r14.2.0 已完成目标设备安装、完整重启、SystemUI、桌面和常用功能回归；实际耗电与长期稳定性仍应在相同 ROM、作用域和使用条件下持续观察。

### 实机与日志验证

- 用户完成最新版安装、完整重启和关注功能测试，应用及常用 Hook 未发现异常。
- 最终归档 `LSPosed_2026-07-23T07_12_24.767916` 中，07:10 完整启动后的 8 次本模块作用域加载全部显示 `Loaded module tv.withaibuild.customiuizer.r14 successfully`。
- 当前启动周期内 `[Pengeek]` 异常、应用/SystemUI/桌面崩溃、ANR 与进程死亡均为 0；SystemUI 与桌面进程从启动到日志结束保持存活。
- `log.old` 在 07:09:01 记录过一组集中式 `Failed to hook`，发生在安装编译记录之后、完整重启之前；07:10 正常启动没有复现，对应功能实测正常，因此不计为当前版本运行时缺陷。
- 日志中的微信、钉钉等 Hook 异常来自其他模块；Vector 启动期 Binder、SELinux 和厂商缺失库提示也没有本模块调用栈。
- 正式 APK 与实测候选的 DEX、资源和原生库内容一致，仅移除了 120 B 的构建期 Git 修订元数据；不改变任何运行代码或 Hook 行为。

### 构建产物验证

- 版本：`versionCode 118` / `versionName r14.2.0`。
- APK：`CustoMIUIzer-A14-r14.2.0.apk`，2,886,165 B。
- SHA-256：`ecb26c58358bc80c99def27981df3e8d313b405662076974489e0bd81adb0b36`。
- 通过 Release 单元测试、R8、资源压缩、zipalign、APK v2 签名、`lintRelease` 与 `lintVitalRelease`；3 项单元测试全部通过，Lint 为 0 个错误、448 个既有私有/废弃 ROM API 兼容性警告。
- 签名证书 SHA-256：`3061a3da1c2fc46b44e215d024b1bfe3a012cb4d70b90b0214fa9fc896cef60d`，与 r14.1.3 一致，可直接覆盖升级。
- 目标设备实机与最终日志验证通过，可作为 r14.2.0 正式发布产物。

## r14.1.3 — 稳定性修复、轻量化与资源治理（合并 r14.1.0-r14.1.3）

发布日期：2026-07-23。状态：**稳定版，已完成目标设备实机验证**。

### API 101 与 Hook 稳定性

- 修复 Release 混淆后全部 `after` Hook 被跳过的问题。旧适配层依赖会被 R8 改写的回调方法名；现在改为按返回类型和参数签名识别。
- 恢复依赖后置回调的 Launcher 初始化、最近任务背景模糊与清理按钮、控制中心运营商隐藏、主题样式和图标颜色更新。
- 保持 `GlobalActions`、`Controls`、`Launcher`、`System`、`Various` 使用原生 `intercept(Chain)`；SystemUI 继续使用已验证兼容层，避免再次扩大迁移风险。
- 保持 Xposed 回调类型与 AndroidX 普通启动链隔离，修复“重启后 Hook 生效，但设置应用打不开”的回归。
- 修复 SystemUI 开机创建移动网络图标时，`mState` 尚未赋值便被双卡信号 Hook 读取的问题；缺失状态、视图或资源时直接放行原方法。
- Android 14 动态广播显式设置导出标志；Wi-Fi 扫描结果读取增加权限拒绝兜底。

### 代码与资源优化

- 将 4 类应用列表各自创建的无界线程池合并为一个共享有界池：2–4 个后台线程、15 秒空闲回收、最大等待队列 128。
- 将图标 LRU 缓存由“最多占 Java 堆的一半”改为 1–16 MiB 的有界缓存，降低内存峰值和回收压力。
- 移除低内存路径中的主动 `Runtime.gc()`，避免人为制造全局停顿。
- 音频可视化的静音 FFT 判断由每个频段一次改为每帧一次；31 个频段的静音帧最多从 31 次扫描降为 1 次。
- 专辑图重复检查不再在主线程逐像素调用 `Bitmap.sameAs()`。
- 常量 Hook、反射和资源访问沿用已建立的缓存/快速路径，不新增后台服务、定时任务或持续轮询。

### 轻量化

- 移除“支持”区域中的版本下载、代码仓库、微信与 PayPal 赞赏入口。
- 删除仅服务于这些入口的 WebView 页面、布局、图片、菜单和多语言文案。
- 移除应用的 `INTERNET` 权限；保留开机广播、蓝牙、Wi-Fi、跨用户和 Hook 所需权限。
- 删除上游赞助配置与失效的 Crowdin 工作流。

### 实机与日志验证

- 用户已验证应用可打开、卸载重装后可用、完整重启后 Hook 正常。
- 已验证最近任务背景模糊/清理按钮、控制中心运营商隐藏、控制中心主题和图标颜色等此前回归功能。
- 首轮日志曾记录双卡信号视图初始化期间 21 次同源空指针，已映射到 `SystemUI.java`；增加状态、视图和资源空值保护后不再复现。
- 最终复验归档 `LSPosed_2026-07-23T02_46_07.858483` 包含 02:41 与 02:43 两轮启动记录，两轮 `[Pengeek]` 异常均为 0，各作用域进程均显示模块加载成功。
- 当前启动周期中 SystemUI、桌面和设置应用均只启动 1 次、进程死亡 0 次；设置应用成功进入前台，未发现本应用或 SystemUI 的崩溃、ANR 和隐性重启。
- SELinux 中的 Xiaomi 显示属性/RenderThread 探测也出现在 Gmail、DocumentsUI 等普通应用；微信、钉钉和 XSmsCode 的 Hook 报错属于其他模块，均不计入本项目缺陷。
- 最终结论：r14.1.3 通过安装、启动、完整重启、常用功能与重启日志闭环验证，可作为稳定发布版本。

### 相对 r14.1.2 的静态评估

| 指标 | r14.1.2 | r14.1.3 | 结论 |
|---|---:|---:|---|
| APK 大小 | 2,934,628 B | 2,886,250 B | 减少 48,378 B（1.65%） |
| 应用网络权限 | 有 | 无 | 删除模块自身联网能力 |
| 图标执行器 | 每个 Adapter 独立、队列无界 | 单个共享有界池、队列 128 | 限制线程竞争与任务积压 |
| 图标缓存 | 最大堆的 1/2 | 1–16 MiB | 限制内存峰值 |
| 主动 GC | 低内存时调用 | 已移除 | 降低人为停顿风险 |
| 静音 FFT 判断 | 每频段一次 | 每帧一次 | 减少空闲帧计算 |
| Hook 架构 | 已验证的 API 101 混合架构 | 保持边界并修复 R8 回归 | 不扩大兼容面 |

这是代码路径与构建产物对比，不等同于实机功耗跑分。实际耗电、启动耗时和兼容性仍应在同设备、同 ROM、同作用域下长期对照。

### 构建产物验证

- 通过 `clean assembleRelease lintRelease lintVitalRelease`，无阻断性 Lint 错误。
- 通过 R8、资源压缩、zipalign、APK v2 签名与证书一致性检查。
- 版本：`versionCode 117` / `versionName r14.1.3`。
- APK：`CustoMIUIzer-A14-r14.1.3.apk`，2,886,250 B。
- SHA-256：`17d1f71607e06e5beb7939c17819932e558bd34c622f369ea87bebfe7b0eba57`。

### r14.1.0 累积变更

- 让 `MethodHook` 直接实现 `XposedInterface.Hooker`，使用 `intercept(Chain)` 调度。
- 首批迁移 `GlobalActions` 与 `Controls`；其他模块暂由兼容层承接。
- 保留旧 Hook 的可变参数、提前返回、结果替换、异常传播与后置回调语义。
- 建立逐模块迁移、构建、重启、回归验证流程，避免一次性重写整个 Hook 层。

### r14.1.1 累积变更

- 将 `Launcher`、`System`、`Various` 分模块迁移至 `intercept(Chain)`，每步执行重启验证。
- `SystemUI` 全量迁移后出现重启失效，因此回退到兼容层；这一实机结论成为后续版本的稳定性边界。
- `HookBuilder` 显式使用 `ExceptionMode.PASSTHROUGH`，保持被 Hook 方法的异常传播语义。
- 完成 clean build、zipalign 与 APK v2 签名验证。

### r14.1.2 累积变更

- 恢复到用户确认可打开应用、重启后可正常 Hook 的 Devin 最终构建，作为 r14.1.3 开发的干净基线。
- 固定 API 101 混合架构边界：`GlobalActions`、`Controls`、`Launcher`、`System`、`Various` 使用原生拦截器，SystemUI 保留兼容层。
- 保持普通应用启动链与 Hook API 隔离，避免 AndroidX 初始化直接加载仅存在于注入环境的类型。
- `versionCode 116`，APK 2,934,628 B。
- SHA-256：`a46acee41da42c618ee0f23468bb37574faedbfb4f9a5df6b26b678106dd32ea`。

## r14.0.0 — Android 14 / API 101 独立版本线

发布日期：2026-07-20。

- 以 MonwF/customiuizer `v24.10.12` 为 Android 14 功能参考，建立独立维护、构建和发布版本线。
- 将 Hook 接口更新到 libxposed API 101，并把初始化范围限制为 Android 14，避免向 Android 15/16 的未知系统组件注册。
- applicationId 调整为 `tv.withaibuild.customiuizer.r14`，与参考版本的安装身份区分。
- 完成首轮性能整理：类与参数缓存、Context/资源复用、主题值预解析、常量 Hook 快速路径、依赖实例缓存和资源 Hook 早退。

## 发布原则

- 安装前备份设置，不同时启用两个同源模块。
- 每次升级后先打开应用，再完整重启设备并验证常用 Hook。
- 未经目标设备确认的构建只能作为预发布候选，不覆盖最近稳定产物。
- Release 必须列出版本、包名、哈希、签名方案、验证范围和已知限制；发布标题只使用版本号。