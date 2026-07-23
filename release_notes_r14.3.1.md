## r14.3.1 — 锁屏充电数据去重

发布日期：2026-07-24。状态：**候选发布版，构建产物通过测试与打包，实机验证待完成后正式发布**。

### 锁屏充电数据

- `System.ChargingInfoHook` 新增 `isKeyguardIndicationCaller()` 栈追踪判断，仅对 `KeyguardIndicationController` 调用路径修改 `ChargeUtils.getChargingHintText` 返回结果，避免充电动画/充电视图等其他调用方显示同一行数据。
- 增加 `isChargingInfoHooked` 静态防护，防止同一进程内重复注册 `getChargingHintText` Hook。
- 在拼接信息前检查 `hint.contains(info)`，若原始提示文本已包含相同数据则不再追加，杜绝同一字符串内出现两行重复充电数据。

### 构建产物验证

- `versionCode` 130 / `versionName` r14.3.1。
- APK：`CustoMIUIzer-A14-r14.3.1.apk`，2,886,165 bytes，SHA-256: `E8DA64B17CBF2E409C562CAAD0C011A575AA9B05DE2A3682AA3F5C4538417FDE`。
- 通过 `gradlew assembleRelease`；`lintRelease` 0 错误，既有 ROM API 兼容性警告保持。
- 签名证书与 r14.3.0 一致，可直接覆盖升级。
