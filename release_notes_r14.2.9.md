## r14.2.9 — 生命周期与绘制热路径

发布日期：2026-07-23。状态：**稳定版，构建产物、单元测试与实机完整重启均通过，LSPosed 日志确认无 CustoMIUIzer 相关崩溃或异常**。

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
- 签名证书与 r14.2.8 一致，可直接覆盖升级。
