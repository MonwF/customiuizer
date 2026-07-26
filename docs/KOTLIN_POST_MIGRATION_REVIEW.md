# Kotlin 重构后二次审查规范

## 目标

r14.9.0 已完成 System、SystemUI、Launcher、GlobalActions、Controls、Various、
PackagePermissions 等核心 mods 的 Kotlin 迁移。本阶段不再追求机械式“全项目 Kotlin 化”，
而是验证迁移后的 JVM 行为、Hook 语义、生命周期、R8 和性能边界。

固定优先级：

> 实际可运行 > 行为正确 > API 101 稳定 > API 102 兼容 > 性能功耗 > 可维护性 > Kotlin 形式。

## 一、允许保留 Java

以下情况可保留最小 Java 边界：

- Xposed 入口或动态加载的 JVM 结构已经稳定；
- Java/Kotlin 互操作比迁移后更清晰可靠；
- 迁移会改变静态入口、构造器、属性访问器或初始化时机；
- 反射、DexKit、Manifest/XML、R8 依赖现有字节码结构；
- 没有实际维护、性能或安全收益。

不得把 100% Kotlin 当作验收指标。

## 二、逐类审查

重点检查：

- `object`、`companion object`、顶层函数和 Java `static` 是否等价；
- `@JvmStatic`、`@JvmField`、`@JvmName` 是否完整；
- public/protected/internal/private 是否改变动态访问；
- nullable/platform type、强转和 `!!` 是否引入异常；
- 属性 getter/setter 是否改变方法名或可见性；
- 默认参数和重载是否改变 Java 调用；
- 初始化顺序是否影响 Hook 安装；
- synchronized/volatile/atomic 语义是否保持；
- Kotlin lambda/匿名对象是否改变 callback 类型；
- R8 优化后 Hooker、after callback 和动态入口是否仍可达。

## 三、Hook 语义

必须保持：

- Hook 目标成员；
- Hook priority；
- 注册条件和顺序；
- before/after 调用顺序；
- 参数修改；
- 提前返回和抛出；
- 原方法异常传播；
- after-hook 结果/异常恢复；
- ClassLoader 和进程边界。

参数未读取时不要复制完整参数数组；只有修改参数时才创建替代数组。

`HookerClassHelper` 是稳定兼容边界，除非发现明确问题，不进行架构性重写。

## 四、性能审查

只修复有明确成本的问题：

- 关闭功能仍安装 Hook；
- 无关进程初始化功能；
- 重复 Hook/Receiver/Observer/Listener；
- 永久轮询或延迟循环；
- 生命周期结束后任务未取消；
- 静态持有 UI/Context/ClassLoader；
- 无界缓存；
- 高频反射、DexKit、Binder、磁盘和日志；
- 绘制、动画、触摸和状态刷新中的明显临时分配；
- 为 Kotlin 形式引入的集合链、Sequence、Flow、装箱或闭包捕获。

不能解释减少何种系统成本的修改不做。

## 五、API 101/102 结合审查

API 102 编译后，重点确认：

- API 101 公共路径不依赖 API 102 专属类型；
- 版本判断不进入热路径；
- 设置应用不解析 compileOnly libxposed 类型；
- `targetApiVersion=102` 后无 Legacy API；
- API 102 新生命周期不会造成重复初始化；
- Hot Reload 仍保持关闭；
- R8 keep 规则只保留必要边界。

## 六、验证

每个关键变更至少：

1. 检查局部 diff；
2. 编译；
3. 运行相关测试；
4. 完整 Release/R8 构建；
5. 检查动态入口和 APK 元数据；
6. 列出 API 101 与 API 102 实机测试点。

不能用 Debug 编译通过替代 Release 和实机验证。
