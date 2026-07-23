## r14.2.10 — 功能关闭零成本与线程统一

发布日期：2026-07-23。状态：**候选发布版，构建产物通过测试与打包，实机验证待完成后正式发布**。

### 减少无效 Hook 与资源替换

- 新增 `SystemUI.hasStatusBarModifications()`，汇总 `setupStatusBar` 中所有资源替换与状态栏文本图标开关条件，包括状态栏边距、控制中心样式、音量计时器、图标大小、步数显示、抽屉日期、点击解锁、锁屏超时、电池/设备温度等。
- `MainModule` 在 `SystemUIInitializer.init` 中仍注册初始化 Hook，但 `SystemUI.setupStatusBar` 内部先判断 `hasStatusBarModifications()`；无任何相关功能开启时，仅写入 `systemui_restart_time` 标记，不调用 `addFakeResource` 和 `setThemeValueReplacement`/`setResReplacement`。
- 避免 SystemUI 每次启动时加载 `statusbar_text_icon` 假资源，以及反复替换 `status_bar_padding_top` 等默认资源，降低功能关闭时的内存与跨进程写入开销。

### 统一线程与生命周期治理

- `WeatherDataController` 不再在每次 `TIME_TICK` 触发时 `new Thread()` 查询天气，改为进程内单一 `ExecutorService` 队列后台查询，减少每分钟新建线程与栈内存抖动。
- `WeatherDataController` 的 `Handler` 改为静态 `Looper.getMainLooper()` 实例，避免 `forceRefresh` 分支重复创建 `new Handler`。
- `WeatherDataController` 在 `initContext` 重复进入时先 `unregisterReceiver` 旧 `TIME_TICK` 接收者，再注册新接收者，并显式指定 `Context.RECEIVER_NOT_EXPORTED`。

### 构建产物验证

- `versionCode` 128 / `versionName` r14.2.10。
- APK：`CustoMIUIzer-A14-r14.2.10.apk`，2,886,169 bytes，SHA-256: `43fd2c26632709604c4ff271b0e40e10a26b001ec681184e022290ff583ea291`。
- 通过 `gradlew test` 与 `gradlew assembleRelease`；3 项单元测试通过，`lintRelease` 0 错误、既有 ROM API 兼容性警告保持。
- 签名证书与 r14.2.9 一致，可直接覆盖升级。
