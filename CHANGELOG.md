# 更新日志

本文件只记录 **CustoMIUIzer A14** 的独立发布线。项目以 [MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) 为 Android 14 功能参考，后续版本围绕两条主线演进：

- 将 Hook 基础设施迁移并稳定在 **libxposed API 101**。
- 在不扩大兼容风险的前提下优化代码、线程、缓存、资源与异常边界。

性能结论会区分静态分析与实机验证；未做同设备功耗采样时，不使用推测性续航或速度百分比。

## r14.7.2 — UI 适配器/偏好设置/小工具 Kotlin 化与界面性能优化

发布日期：2026-07-25。状态：**重构版（仅内部实现迁移，不修改 Hook 行为；需实机验证以下功能）**。

> 本版本在 `r14.7.1` 基础上继续将设置 UI 相关的 Java 类迁移为 Kotlin，并针对列表滑动、颜色选择、偏好控件做性能优化。未修改 Xposed Hook 核心路径。

### 变更摘要与对应测试功能

- **UI 适配器 ViewHolder 化**（`utils`）
  - `ResolveInfoAdapter`、`AppDataAdapter`、`PrivacyAppAdapter`、`LockedAppAdapter`、`PreferenceAdapter` 全部改用 `ViewHolder`，复用 `convertView`，避免重复 `findViewById`。
  - 对应功能：**所有应用/活动/隐私/锁定/偏好列表**。快速滑动时应无卡顿、无图标错位。
- **UI 控件 Kotlin 化**（`utils/prefs/subs`）
  - `ColorCircle.kt`：颜色选择圆环，启用硬件图层，减少颜色轮分配。
  - `SortableListView.kt`：可拖拽排序列表，迁移触摸/动画逻辑。
  - `SpinnerEx.kt` / `SpinnerExFake.kt`：下拉选择控件，反射设置弹窗高度。
  - `PreferenceEx.kt` / `SeekBarPreference.kt` / `ListViewEx.kt` / `ColorPreferenceEx.kt` / `EditTextPreferenceEx.kt` / `PreferenceCategoryEx.kt` / `CheckBoxPreferenceEx.kt` / `DropDownPreferenceEx.kt` / `ListPreferenceEx.kt`：所有偏好控件 Kotlin 化。
  - `GetPathUtils.kt`：移除大量废弃注释代码，精简目录 URI 解析。
  - 对应功能：**设置主页面、子页面、颜色选择器、下拉框、滑块、文本编辑等**应正常显示与交互。
- **子页面 Kotlin 化**（`subs`）
  - `CategorySelector`、`Controls`、`Launcher`、`System`、`System_*`、`Various`、`Various_*` 全部迁移。
  - 对应功能：**系统/桌面/控制/各类设置子页面**应正常进入、保存与返回。
- **PreferenceState 接口 Kotlin 化**
  - `PreferenceState.java` → `PreferenceState.kt`；所有实现类同步调整。

### 基础设施与性能优化

- 列表适配器统一实现 `ViewHolder` 模式，减少滚动时 `findViewById` 与 `inflate` 次数。
- `ColorCircle` 设置 `LAYER_TYPE_HARDWARE`，降低复杂颜色选择时的重绘开销。
- 继续使用 `kotlinx-coroutines-android:1.6.4` 与 Kotlin 2.2.21。
- 新增 Kotlin 源文件若干，删除对应 Java 源文件若干（`ColorCircle`、`SortableList`、`SortableListView`、`SpinnerEx`、`SpinnerExFake`、`PreferenceEx`、`SeekBarPreference`、`GetPathUtils`、`ListViewEx`、`ColorPreferenceEx`、`EditTextPreferenceEx`、`PreferenceCategoryEx`、`CheckBoxPreferenceEx`、`DropDownPreferenceEx`、`ListPreferenceEx`、`System` 主片段、全部小 `subs`、`PreferenceState`）。

### 构建产物验证

- `versionCode` 167 / `versionName` r14.7.2。
- APK：`CustoMIUIzer-A14-r14.7.2.apk`，3,048,379 bytes，SHA-256: `C9BB1BA068710FABEBEA0235DE3551AD3E6EE7E27B55FA94958BB43F27C933A4`。
- 通过 `./gradlew assembleRelease`；`lintVitalRelease` 0 错误，仅有 ROM API 兼容性既有警告。
- 签名证书与 `r14.7.1` 一致；包名不变，可覆盖安装。

## r14.7.1 — Kotlin 协程化继续迁移（设置/应用选择子页面）

发布日期：2026-07-25。状态：**重构版（仅内部实现迁移，不修改 Hook 行为；需实机验证以下功能）**。

> 本版本在 `r14.7.0` 基础上继续将设置子页面与列表选择相关 Java 类迁移为 Kotlin，并用 Kotlin Coroutines 替代 `Thread` / `Handler.postDelayed`。未修改 Xposed Hook 核心路径。

### 变更摘要与对应测试功能

- **`ActivitySelector.kt`**（`subs`）
  - 原异步机制：`new Thread` + `runOnUiThread`
  - 对应功能：**应用活动选择界面**。进入「选择活动」时应正常列出包内 Activity 并可选中返回。
- **`AppSelector.kt`**（`subs`）
  - 原异步机制：`new Thread` + `runOnUiThread` + `BroadcastReceiver`
  - 对应功能：**应用/分享/打开方式/隐私/锁定/自定义标题等应用列表选择界面**。列表加载、搜索过滤、点击选中、隐私配置广播接收均应正常。
- **`SubFragmentWithSearch.kt`**（`tv.withaibuild.customiuizer`）
  - 无原异步机制，纯 Java → Kotlin 迁移。
  - 对应功能：**带搜索框的应用列表子页面**。搜索框焦点、键盘隐藏、实时过滤应正常。
- **`SubFragment.kt`**（`tv.withaibuild.customiuizer`）
  - 原异步机制：`postDelayed` 自动滚动高亮项。
  - 对应功能：**所有设置子页面基类**。从主页面跳转至深项时自动滚动高亮应正常；同时保持 `openAppsEdit`、`openLockedAppEdit` 等公开监听器字段，供 `System`、`Launcher`、`Controls` 等子类继续使用。
- **`MainFragment.kt`**（`r14.7.0` 未纳入，本版本补齐）
  - 原异步机制：`new Thread`（`Helpers.getAllMods`） + `Handler.postDelayed`
  - 对应功能：**主设置页面加载、搜索、Xposed 未激活提示**。主页面不应出现启动卡顿，未激活提示应在约 800ms 后正确弹出。

### 基础设施变更

- 继续使用现有 `kotlinx-coroutines-android:1.6.4` 与 Kotlin 2.2.21。
- Fragment 中统一使用 `lifecycleScope`（来自 `androidx.lifecycle:lifecycle-runtime-ktx`，已通过传递依赖引入）管理协程生命周期，避免手动 `CoroutineScope.cancel()`。
- 新增 Kotlin 源文件 5 个，删除对应 Java 源文件 5 个（`MainFragment`、`SubFragment`、`SubFragmentWithSearch`、`AppSelector`、`ActivitySelector`）。
- 子类仍从 Java 访问 `SubFragment` 的 `protected` 字段与可覆盖方法，已通过 `@JvmField` / `open` 保留兼容性。

### 构建产物验证

- `versionCode` 166 / `versionName` r14.7.1。
- APK：`CustoMIUIzer-A14-r14.7.1.apk`，3,031,995 bytes，SHA-256: `4613D886E98E7EB6A5EBE087CEA420B60583B55DC0DC0747D1DE8C153CF5FED1`。
- 通过 `./gradlew test` 与 `./gradlew assembleRelease`；`lintVitalRelease` 0 错误，仅有 ROM API 兼容性既有警告。
- 签名证书与 `r14.7.0` 一致；包名不变，可覆盖安装。

## r14.7.0 — Kotlin 协程化重构（Java → Kotlin 移植）

发布日期：2026-07-25。状态：**重大重构版（仅内部实现迁移，不修改 Hook 行为；需实机验证以下功能）**。

> 本版本将 `r14.6.4` 中多个使用 Java 并发/异步机制的模块移植为 Kotlin，并用 Kotlin Coroutines 替代 `ThreadPoolExecutor` / `ExecutorService` / `Handler` / `AsyncTask` / `postDelayed`。所有改动对外 API 保持不变，理论上不影响功能，但建议重点测试以下对应模块。

### 变更摘要与对应测试功能

- **`BitmapCachedLoader.kt`**（`utils`）
  - 原异步机制：`ThreadPoolExecutor` + `Handler`
  - 对应功能：**应用选择/锁定/隐私列表中的图标加载**，例如 `AppSelector`、`LockedApp`、`PrivacyApp`、`ResolveInfoAdapter`。进入这些设置页面时应能看到应用图标正常加载、无错位。
- **`WeatherDataController.kt`**（`mods/utils`）
  - 原异步机制：`ExecutorService` + `Handler`
  - 对应功能：**锁屏/时钟天气显示**。开启天气相关设置后，锁屏或状态栏天气应正常刷新。
- **`StepCounterController.kt`**（`mods/utils`）
  - 原异步机制：`Handler` + `BroadcastReceiver`
  - 对应功能：**锁屏步数显示**。开启健康/步数相关设置后，锁屏步数应正常更新。
- **`AudioVisualizer.kt`**（`utils`）
  - 原异步机制：`AsyncTask` + `Handler`
  - 对应功能：**锁屏/抽屉音乐可视化条**。播放音乐时，可视化条应随节奏正常跳动。
- **`BatteryIndicator.kt`**（`utils`）
  - 原异步机制：`postDelayed` / `post`
  - 对应功能：**状态栏顶部/底部彩色电量条**。电量变化、充电状态切换时颜色与长度应正确。
- **`BTList.kt` / `WiFiList.kt`**（`subs`）
  - 原异步机制：`Handler` + `postDelayed`
  - 对应功能：**设置中的蓝牙/WiFi 触发条件选择界面**。扫描设备、点击列表项增删应正常。

### 基础设施变更

- 根目录 `build.gradle` 引入 `org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21`。
- `app/build.gradle` 应用 `org.jetbrains.kotlin.android` 插件，并增加 `kotlinx-coroutines-android:1.6.4` / `kotlinx-coroutines-test:1.6.4` 依赖。
- Kotlin 编译目标 Java 17，与现有 Java 源码完全互操作（`@JvmStatic` / `@JvmField` / `@JvmOverloads` 等保留）。
- 新增 Kotlin 源文件 7 个，删除对应 Java 源文件 7 个，未修改 Xposed Hook 核心路径。

### 构建产物验证

- `versionCode` 165 / `versionName` r14.7.0。
- APK：`CustoMIUIzer-A14-r14.7.0.apk`，3,031,995 bytes，SHA-256: `286EEBBEC81AE1E622C634D75E15F9BBCE9DD8BF0CC24E088E6BD61B070E037C`。
- 通过 `./gradlew test` 与 `./gradlew assembleRelease`；`lintVitalRelease` 0 错误，仅有 ROM API 兼容性既有警告。
- 签名证书与 `r14.6.4` 一致；包名不变，可覆盖安装。

## r14.6.4

发布日期：2026-07-25。状态：**稳定版（r14.6.x 终版；合并 r14.6.2、r14.6.3；双排信号栏修复与生命周期治理已实机验证）**。

> `r14.6.2` 与 `r14.6.3` 已废弃并合并到本版本，对应 release/tag 已删除。本版本在 `r14.6.3` 基础上做最终收尾整理，未改动 Hook 逻辑。

### r14.6.x 演进

- **r14.6.0 原始尝试**：
  - D 类废弃 API/权限迁移：将 `Settings.System` 自定义键（`systemui_restart_time`、`last_music_paused_time`、`dark_mode_enable_by_setting`）迁移到模块 `SharedPreferences`；新增 `Helpers.getSystemSharedPrefs()`。
  - PendingIntent flag 兼容辅助：在 `Helpers` 中新增 `getMutableActivityPendingIntent` / `getImmutableActivityPendingIntent`。
  - 通知渠道：`MainApplication.onCreate` 创建默认低重要性 `NotificationChannel`（ID `customiuizer_default`）。
  - 权限声明：`AndroidManifest.xml` 新增 `WAKE_LOCK` 与 `POST_NOTIFICATIONS`。
- **r14.6.1 热修复**：
  - 回退 `Settings.System` 自定义键迁移，恢复使用 `Settings.System`；移除 `Helpers.getSystemSharedPrefs()`，避免 `onPackageReady` 早期 `Context` 尚未绑定到应用数据目录时调用 `getSharedPreferences` 触发 `RuntimeException: No data directory found for package android`。
  - 保留通知渠道、`WAKE_LOCK`/`POST_NOTIFICATIONS` 权限声明、PendingIntent flag 兼容辅助。
  - 实机验证确认无模块相关报错。
- **r14.6.2 清理与发布**：
  - 删除 GitHub `r14.6.0` 和 `r14.6.1` release 与 tag（两个版本均已废弃或合并）。
  - 合并 r14.6.0/14.6.1 变更记录到本版本。
  - 移除 `Helpers.java` 中未使用的 `android.content.SharedPreferences` 导入。
  - 移除 `AndroidManifest.xml` 中未使用的 `POST_NOTIFICATIONS` 权限声明；`WAKE_LOCK` 仍保留（`Controls.java` 实际使用 `WakeLock`）。
  - 重写 `README.md`，新增“各版本全方位对比”表。
- **r14.6.3 最终修复**：
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
  - **双排信号栏 SIM1 信号为空修复**：`SystemUI.DualRowSignalHook` 不再把 `MobileIconState.strengthId` 覆盖成等级数值，保留原始信号 drawable 资源 ID；在 `applyDarknessInternal` 与 `onDarkChanged` 中通过 `Resources.getResourceName` 动态解析资源名得到等级，并绘制双排自定义图标。修复因 `strengthId` 被改为 1..5 后被 SystemUI `setImageResource` 当作无效资源 ID 加载，导致 SIM1 信号为空的问题。
  - **双排信号栏图标颜色跟随状态栏**：`applyDualSignalDrawables` 在设置自定义信号图标后，从 `mMobileRoaming` 读取当前 `ImageTintList`（未取到则按 `mLight` 回退黑白），并同步设置到 `mMobile` 与 `mSmallRoaming`，修复状态栏图标变黑/变白时双排信号图标颜色不跟随的问题。
- **r14.6.4 最终整理**：
  - **大文件拆分（俄罗斯代码风格；保持原类委托 stub，调用方不变）**：
    - `System.java`：提取状态栏/时钟相关 hooks 到 `SystemClockHooks.java`、`SystemStatusBarBackgroundHooks.java`、`SystemStatusBarIconHooks.java`。
    - `SystemUI.java`：提取电池相关 hooks 到 `SystemUIBatteryHooks.java`。
    - `GlobalActions.java`：提取意图启动/解析逻辑到 `GlobalActionsIntentHelper.java`。
    - 移除 `System.java` 中废弃的注释代码块。
  - **单元测试**：
    - `PrefMapTest`：补充 `getStringAsInt` 缓存与失效测试。
    - `XposedHelpersCacheTest`：补充 `Constructor` 缓存/匹配测试。
    - `AppHelperTest`：补充 clearType=0、非数字字符串、缺失 needle 等边界测试。
  - **Proguard/R8**：
    - `proguard-rules.pro` 补充 `MainActivity`、`Credentials`、`MainApplication` 等 manifest 组件的 `-keepnames`。
  - **依赖检查**：
    - 通过 `dependencyUpdates` 检查，当前配置仓库中无合适的安全稳定升级项，未升级 alpha/beta 依赖。

### 构建产物验证

- `versionCode` 164 / `versionName` r14.6.4。
- APK：`CustoMIUIzer-A14-r14.6.4.apk`，2,949,361 bytes，SHA-256: `E7E6A23A04E709DF269DF1087FB3128435F532CF35BE53C1FE051595249B3280`。
- 通过 `gradlew test` 与 `gradlew assembleRelease`；`lintVitalRelease` 0 错误，既有 ROM API 兼容性警告保持。
- 签名证书与 r14.5.0 一致；包名不变，可覆盖安装。

### 实机验证

- 完整重启后，无模块相关 `AndroidRuntime`/`FATAL`/`am_crash`/`am_anr`；`tv.withaibuild.customiuizer.r14` 包正常安装，系统服务与 KernelSU 策略加载无异常。
- 控制中心关闭/开启移动数据后，双排信号栏 SIM1 信号显示正常；状态栏图标变黑/变白时双排信号图标颜色正确跟随。
- 日志中存在的 `Invalid resource ID 0x00000000`（`ClockPalette`）与 `ApkAssets` 弱引用警告均不含本模块调用栈，未追溯到 CustoMIUIzer 代码。
- 建议后续再抓一次开启全部作用域后的启动日志，以补全 r14.6.4 的加载数据。

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

- 完整重启后，模块 binder 已正常注册，无模块相关 `AndroidRuntime`/`FATAL`/`am_crash`/`am_anr`。
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

- 模块加载正常，无模块相关 `AndroidRuntime`/`FATAL`/`am_crash`/`am_anr`。
- SystemUI、Launcher、Settings 等目标进程无 CustoMIUIzer 引发的崩溃、ANR 或异常栈。

## r14.2.9 — 生命周期与绘制热路径（合并 r14.2.8-r14.2.9）

发布日期：2026-07-23。状态：**稳定版，实机验证确认无 CustoMIUIzer 相关崩溃或异常**。

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

发布日期：2026-07-23。状态：**稳定版，实机验证确认无 CustoMIUIzer 相关崩溃或异常**。（r14.2.5 与 r14.2.6 因状态栏过渡异常已回退并删除 tag/release，r14.2.7 为 r14.2.4 之后的下一个稳定版本）

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

发布日期：2026-07-23。状态：**稳定版，实机验证确认无 CustoMIUIzer 相关崩溃或异常**。

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

### 实机验证

- 用户完成最新版安装、完整重启和关注功能测试，应用及常用 Hook 未发现异常。
- 多轮完整启动后，各作用域进程模块加载成功。
- 当前启动周期内 `[Pengeek]` 异常、应用/SystemUI/桌面崩溃、ANR 与进程死亡均为 0；SystemUI 与桌面进程从启动到日志结束保持存活。
- 安装后、完整重启前可能出现一次性 Hook 失败记录，完整重启后未复现，不计为运行时缺陷。
- 应用级 Hook 异常来自其他模块；系统启动期 Binder、SELinux 和厂商缺失库提示均不含本模块调用栈。
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

### 实机验证

- 用户已验证应用可打开、卸载重装后可用、完整重启后 Hook 正常。
- 已验证最近任务背景模糊/清理按钮、控制中心运营商隐藏、控制中心主题和图标颜色等此前回归功能。
- 首轮日志曾记录双卡信号视图初始化期间 21 次同源空指针，已映射到 `SystemUI.java`；增加状态、视图和资源空值保护后不再复现。
- 多轮完整启动后，未出现异常，各作用域进程模块加载成功。
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