# 来源与修改说明

## 项目身份

**CustoMIUIzer A14（应用名：米客 A14）** 是由 `tomthenpc` 独立维护的下游衍生项目，不是 Mikanoshi 或 MonwF 发布、认可或提供支持的官方版本。

## 来源

- 原始项目：CustoMIUIzer，作者 Mikanoshi 及其贡献者
  <https://code.highspec.ru/Mikanoshi/CustoMIUIzer>
- Android / HyperOS 上游：MonwF/customiuizer，作者 MonwF 及其贡献者
  <https://github.com/MonwF/customiuizer>
- 本项目的 Android 14 功能基线参考上游 `v24.10.12`；后续修改由本仓库提交历史记录。

Git 历史保留了原始及上游贡献者的作者信息。第三方源码中的独立版权与许可证声明继续有效。

## 本项目的主要修改

- 面向 HyperOS 1 / Android 14 固定兼容范围。
- 迁移并适配 libxposed API 101 与指定 Vector/LSPosed 基线。
- 独立包名、版本线、构建签名和 Release 流程。
- Hook 兼容、启动隔离、性能、内存、可靠性及资源精简。
- 移除上游下载、仓库、赞赏和内置网页入口。
- 重写项目文档、隐私说明和发布规范。

## 许可证

本项目作为衍生作品继续依据 GNU General Public License v3.0 发布，完整条款见 [LICENSE](LICENSE)。分发二进制版本时，应同时提供与该二进制对应的完整源码和构建所需脚本，保留许可证与版权声明，并清楚标注所作修改。
