# Android 14 / libxposed API 101 维护说明

本版本线源自 MonwF/customiuizer 的 Android 14 工作，并以 `v24.10.12` 作为功能参考。当前项目独立维护，来源与许可见 [NOTICE.md](NOTICE.md)。

## 目标环境

- Xiaomi HyperOS 1 / Android 14（SDK 34）
- 主要测试设备：Xiaomi 13（2211133G）
- `arm64-v8a`
- libxposed API 101
- Vector v2.0-3046，Actions run `29805285935`，commit `9350c7c`

## 稳定性边界

- 仅在 Android 14 初始化 Hook，避免误用于 Android 15/16。
- `GlobalActions`、`Controls`、`Launcher`、`System`、`Various` 使用原生 `intercept(Chain)`。
- `SystemUI` 保留经验证的 `HookerClassHelper` 兼容层，不做全量 Kotlin 或 Hook 架构迁移。
- Release 混淆必须保持普通应用启动链与 compileOnly libxposed 类型隔离。
- 后置回调检测必须基于方法签名，不得依赖会被 R8 改写的方法名。
- 任何 SystemUI 或 Launcher 改动都必须经过“打开应用 → 完整重启 → 再次打开应用 → 验证 Hook”的闭环。

## 标识

- 项目名：CustoMIUIzer A14
- 应用名：米客 A14
- applicationId：`name.monwf.customiuizer.r14`
- Java namespace：`name.monwf.customiuizer`

保留现有 applicationId 是为了兼容当前签名、覆盖安装、LSPosed 模块身份和用户设置。若未来迁移包名，应作为新的主版本单独处理。
