# 项目谱系与参考边界

## 最上游

仓库：

`https://github.com/MonwF/customiuizer`

功能基线：

`v24.10.12`

Release：

`https://github.com/MonwF/customiuizer/releases/tag/v24.10.12`

该版本是本项目 HyperOS 1 / Android 14 功能和原始 Hook 行为的最上游参考。

## 当前独立项目

仓库：

`https://github.com/tomthenpc/customiuizer-a14`

独立化内容包括：

- 包名和 namespace
- applicationId
- provider/组件标识
- 版本号和 Release 线
- 签名与构建流程
- 性能和生命周期治理
- 现代 libxposed API 101 适配
- Java → Kotlin 重构
- 后续 API 101/102 双兼容

## 使用原则

遇到功能回归时，上游可用于核对：

- 功能原意
- Hook 类、方法和参数
- before/after 语义
- 用户可见行为
- ROM 兼容分支的历史来源

但不得：

- 用上游文件覆盖当前 Kotlin 实现
- 恢复旧包名或 authority
- 将当前仓库 reset/rebase/merge 到上游 tag
- 把上游旧构建配置带回
- 用上游测试结果替代当前 R8 和实机验证

技术判断优先级：

1. 当前用户要求
2. 当前独立仓库实际代码与实机结果
3. libxposed 官方资料
4. 上游 v24.10.12 功能语义与历史实现
5. 可能滞后的说明文档
