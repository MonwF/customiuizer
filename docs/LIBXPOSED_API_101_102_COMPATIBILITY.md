# libxposed API 101 / API 102 双兼容说明

## 兼容目标

同一个 r14.10.0 APK 同时面向实现 libxposed API 101 与 API 102 的框架：

```properties
minApiVersion=101
targetApiVersion=102
staticScope=false
```

Android 平台范围保持 `minSdk=34`、`targetSdk=34`，与 libxposed API 版本无关。

## 实现边界

- 使用 `io.github.libxposed:api:102.0.0` 编译，运行最低基线仍为 API 101。
- UI 侧 service 使用 `io.github.libxposed:service:102.0.0`。
- 公共 Hook 路径只调用 API 101 已存在的 `XposedModule` 生命周期、`HookBuilder`、`Hooker.intercept`、`Chain.proceed` 和 `HookHandle.unhook`。
- API 102 新增的 Hot Reload、hook ID 与原子 replacement 本阶段不启用，因此公共加载路径不引用其新增参数类型或方法。
- 不使用反射访问 Xposed API，也不使用 Legacy `de.robv.android.xposed` API。
- Manifest 的 `de.robv.android.xposed.category.MODULE_SETTINGS` 是模块设置入口 category，不是 Legacy Xposed API 调用。

API 101 与 102 AAR 的公开符号对比表明，项目当前使用的接口签名保持不变。API 102 新增 `HotReloadingParam`、`HotReloadedParam`、`onHotReloading`、`onHotReloaded`、`HookBuilder.setId`、`HookHandle.getId` 和 `HookHandle.replaceHook`；这些符号均未进入本项目运行路径。

## 构建工具链

- Gradle Wrapper：`9.5.1`
- Android Gradle Plugin：`9.2.1`
- Android 编译平台 / Build Tools：`37` / `37.0.0`
- Java / Kotlin JVM target：`17`
- Android 运行范围保持 `minSdk=34`、`targetSdk=34`
- Groovy 构建脚本已迁移为 Kotlin DSL；依赖版本由 `gradle/libs.versions.toml` 集中锁定

`service:102.0.0` 的 AAR 元数据要求 `compileSdk >= 37`，因此只提升编译工具链，不扩大本模块支持的 Android 运行版本。

## 静态与构建验证

- 使用最终 API 102 依赖完成 `clean`、单元测试、完整 Lint、Debug 构建和 Release 构建；Release 路径包含 R8、资源压缩和 `lintVitalRelease`
- 临时将依赖切回 `api:101.0.1` / `service:101.0.0` 后，同一份源码通过 `clean test assembleRelease`，随后恢复最终 API 102 配置并重新完整构建
- Release DEX 未发现 Legacy `de.robv.android.xposed` API 描述符，也未发现 API 102 专属 Hot Reload、hook ID 或 replacement 符号
- APK 内 `module.prop`、Xposed 入口、scope、签名和 zip alignment 已单独检查
- 上述结果只证明源码/API 表面兼容与构建产物完整，不能代替 API 101 和 API 102 框架上的冷启动实机验证

## 官方资料

- [libxposed API 102 Javadoc](https://libxposed.github.io/api/)
- [libxposed API 官方仓库与依赖、R8 配置](https://github.com/libxposed/api)
- [libxposed service 官方仓库](https://github.com/libxposed/service)
- [libxposed 官方 example](https://github.com/libxposed/example)

## 实机验收清单

### API 101 框架

- 冷启动后模块成功加载，无 `NoSuchMethodError`、`NoClassDefFoundError`、`AbstractMethodError`、`VerifyError` 或相关类加载异常。
- Remote Preferences 读写正常。
- `system_server`、`com.android.systemui`、`com.miui.home` Hook 正常。
- 设置修改生效；重启目标进程和整机后仍正常。
- 检查日志中没有模块导致的崩溃、ANR 或重复初始化。

### API 102 框架

- 冷启动后模块成功加载，API 101 功能行为一致。
- Remote Preferences 读写正常。
- `system_server`、`com.android.systemui`、`com.miui.home` 无模块相关崩溃。
- 没有 Legacy Xposed API 拒绝、重复 Hook、重复初始化或生命周期异常。
- Hot Reload 保持关闭；本阶段不测试热重载。

两套环境均完成后再创建正式 Release。
