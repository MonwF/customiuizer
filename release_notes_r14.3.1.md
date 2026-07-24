## r14.3.1 — 锁屏充电数据去重、lint 清理与依赖更新

发布日期：2026-07-24。状态：**正式发布版，构建产物已重新生成并覆盖 r14.3.1 包**。

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

### 构建产物验证

- `versionCode` 130 / `versionName` r14.3.1。
- APK：`CustoMIUIzer-A14-r14.3.1.apk`，2,886,165 bytes，SHA-256: `E1ED1FEF9108E9A94D1B532F5B3BCDBD71AF5DC32E610A239CF108A9ABEC57D8`。
- 通过 `gradlew assembleRelease`；`lintRelease` 0 错误，既有 ROM API 兼容性警告保持。
- 签名证书与 r14.3.0 一致，可直接覆盖升级。

### 实机验证

- `LSPosed_2026-07-24T13_52_32.282030` 完整重启后，`VectorModuleManager` 两次加载 `name.monwf.customiuizer.r14` 分别为 6 ms 与 24 ms，日志中无模块相关 `AndroidRuntime`/`FATAL`/`am_crash`/`am_anr`。
- SystemUI、Launcher、Settings 等目标进程无 CustoMIUIzer 引发的崩溃、ANR 或异常栈。
