# 米客 A14（CustoMIUIzer A14）

**米客 A14** 是面向 **HyperOS 1 / Android 14** 的独立维护版系统定制模块。项目以 [MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) 为 Android 14 功能参考，但采用自己的包名、版本线、构建与发布流程。

## 当前版本与推荐

| 项目 | 当前值 |
|---|---|
| 当前稳定版 | **r14.5.0** |
| 上一稳定版 | r14.3.1 |
| 当前候选版 | r14.6.2 |
| 应用名 | 米客 A14 |
| 包名 | `tv.withaibuild.customiuizer.r14` |
| 目标系统 | HyperOS 1 / Android 14（minSdk 34，仅 `arm64-v8a`） |
| Hook 接口 | libxposed API 101 |
| LSPosed 基线 | [Vector v2.0-3046](https://github.com/JingMatrix/Vector/actions/runs/29805285935)，commit `9350c7c` |
| 发布页 | [tomthenpc/customiuizer-a14 Releases](https://github.com/tomthenpc/customiuizer-a14/releases) |

- **推荐日常使用的稳定版**：**r14.5.0**。它已完成包名迁移、`getResId()` 热路径缓存、生命周期加固与完整重启验证，LSPosed 日志无模块相关崩溃/ANR。
- **r14.6.2** 是 r14.6.x 的清理/文档发布，去除了 r14.6.0/r14.6.1 的错误标签，并新增“各版本全方位对比”表。它仅包含死代码与权限清理，无业务逻辑改动，但仍作为**候选版**等待完整功能回归验证。
- **r14.6.0 与 r14.6.1 的 release/tag 已删除**：r14.6.0 因 `systemui` 启动崩溃被废弃，r14.6.1 已合并到 r14.6.2。

> [!WARNING]
> 仅支持 **Android 14（SDK 34）** 与 `arm64-v8a`。不要在 Android 15/16 上启用，也不要与上游版或其他同源分支同时启用，否则可能产生重复 Hook。

---

## 各版本全方位对比

下表覆盖从上游参考到 r14.6.2 的全部发行版，维度包括体积、加载速度、性能/省电、架构、稳定性与推荐度。体积数据来自构建产物，加载时间来自同一设备 LSPosed/Vector 日志，性能结论区分静态分析与实机验证。

| 版本 | 状态 | APK 大小 | 相对上一版 | 模块加载中位数 | 性能/省电 | 架构 | 稳定性 | 推荐度 | 一句话总结 |
|---|---|---:|---:|---:|---|---|---|---|---|---|
| [上游 v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) | 参考基线 | 2,785,364 B | Android 14 基线 | — | 原项目基线 | API 100 | 稳定 | 仅供参考 | 上游最后阶段的 Android 14 功能参考，保留联网权限与支持资源 |
| [r14.0.0](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.0.0) | 初始版 | 2,901,900 B | +116,536 B | — | 体积最大 | 建立 API 101 版本线 | 未完整验证 | 不推荐 | 建立独立版本线，加入类/方法/参数/资源/主题缓存 |
| [r14.1.3](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.1.3) | 稳定版 | 2,886,250 B | −15,650 B | 11.5 ms（首启）/ 8 ms（再启） | 资源治理幅度最大：移除联网权限、支持资源、限制图标缓存 | 有界线程池、locale/Handler 静态清理 | 验证无模块崩溃 | **推荐** | API 101 稳定、资源瘦身最显著的一版 |
| [r14.2.0](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.0) | 稳定版 | 2,886,165 B | −85 B | 8 ms | 偏好热路径、Hook 参数、反射、秒针调度优化 | 长期运行热路径最完整 | 验证无模块崩溃 | **推荐** | 长期运行细节最完整，减少短期对象与重复计算 |
| [r14.2.4](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.4) | 稳定版 | 2,886,165 B | 0 B | 约 8–15 ms | 热路径缓存、反射哨兵、无效 Hook/Receiver 防重注册 | 生命周期治理 | 验证无模块崩溃 | **推荐** | 累积 r14.2.1–2.3 的热路径与反射缓存治理 |
| [r14.2.7](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.7) | 稳定版 | 2,886,165 B | 0 B | 约 8–15 ms | 自定义动作 gate、Launcher gesture gate、ContentObserver/Handler 生命周期 | 功能关闭时真正零成本 | 验证无模块崩溃 | **推荐** | 未配置时不注册 Controller/Receiver，降低功能关闭开销 |
| [r14.2.9](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.9) | 稳定版 | 2,886,165 B | −4 B | 约 8–15 ms | StepCounter 生命周期、BatteryIndicator 绘制热路径缓存、减少 Matrix 分配 | 状态栏刷新对象复用 | 验证无模块崩溃 | **推荐** | 状态栏图标/电池绘制每帧减少对象分配 |
| [r14.3.1](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.3.1) | 稳定版 | 2,886,165 B | 0 B | 15 ms（2 个样本） | 锁屏充电去重、lint 清理、`WeatherDataController` 线程池化 | 功能关闭零成本 | 验证无模块崩溃 | **推荐** | 天气查询不再每分钟 `new Thread()`，功能未开启时跳过资源替换 |
| [r14.5.0](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.5.0) | **稳定版** | 2,886,373 B | +208 B | 约 8–15 ms | 包名迁移后结构统一，`getResId()` 热路径缓存 | 包名/生命周期/缓存/线程池全面落地 | **完整重启验证通过** | **首选稳定版** | 当前最稳基线，签名与包名已统一 |
| r14.6.2（本版） | 候选版 | 2,900,213 B | +13,840 B | 约 8–15 ms | 与 r14.6.1 一致，无业务逻辑改动 | 清理死代码/未使用权限/错误标签 | r14.6.1 日志无报错，本版未改动逻辑 | 候选验证 | r14.6.x 终版：修复 r14.6.0 崩溃、清理 D 类残留、重写 README 对比表 |

> **关于“省电性”的说明**：
> 静态分析表明 r14.2.7 及以后版本通过“功能未开启时不注册 Hook/Receiver/Observer”、减少对象分配与线程池化来降低 CPU/内存/唤醒开销，对续航与流畅度有正面作用。但 APK 大小和加载中位数**不能直接等同于省电收益**；要量化续航改善，必须在同设备、同功能开关、同使用场景下使用 **Perfetto / Batterystats** 长时间对照采样。

### 版本号与质量的关系

- **版本号越大 ≠ 全面更优**。
- r14.1.3 相比 r14.0.0 体积更小且更稳定。
- r14.2.0–r14.3.1 在运行热路径上持续优化，但 APK 体积基本持平。
- r14.5.0 是架构与稳定性最均衡的一版。
- r14.6.x 没有新增用户功能：r14.6.0 引入崩溃，r14.6.1 修复，r14.6.2 清理发布记录与死代码。

---

## 版本选择建议

| 使用场景 | 推荐版本 | 理由 |
|---|---|---|
| 只求稳定、省电、不折腾 | **r14.5.0** | 完整重启验证，无模块崩溃/ANR，架构与缓存都已落地 |
| 想跟随最新验证版 | **r14.3.1** | 功能与稳定兼顾，WeatherDataController 线程池化明显 |
| 重度状态栏/电池绘制定制 | **r14.2.9** | BatteryIndicator 绘制热路径缓存，Matrix 复用 |
| 自定义动作/控制中心开关较多 | **r14.2.7** | 自定义动作 gate 与 Launcher gesture gate，未配置时不加载控制器 |
| 已安装 r14.6.0/14.6.1 并遇到问题 | **r14.6.2** | 修复 systemui 崩溃并清理发布标签，但完整功能验证待补充 |

---

## 与参考版本的区别

| 维度 | 米客 A14 |
|---|---|
| 维护范围 | 固定面向 HyperOS 1 / Android 14，避免向未验证系统注册 Hook |
| Hook 架构 | libxposed API 101；按模块使用原生拦截器或稳定兼容层 |
| 安装身份 | 独立 `applicationId`，可与参考源码和发布历史区分 |
| 性能策略 | 优化高频 Hook、共享有界线程池、限制缓存、减少反射和重复计算 |
| 轻量化 | 移除下载、仓库、赞赏、内置网页与网络权限 |
| 发布策略 | 每版执行混淆构建、Lint、zipalign、签名和实机重启验证 |

## 功能范围

- 状态栏图标、时钟、电池信息和手势
- 通知展开、小窗、频道与音量控制
- 控制中心布局、运营商显示和主题颜色
- 锁屏、自动亮度、蓝牙与 Wi-Fi 信任
- 最近任务、桌面、导航手势与扩展电源菜单
- 系统应用安装、网络限制和其他 HyperOS 调整

功能兼容性取决于 Xiaomi 系统应用和 ROM 的具体版本。未在目标设备验证的组合不作兼容承诺。

## 安装

1. 在当前版本中备份设置。
2. 卸载上游版或其他同源分支，不要保留多个模块同时启用。
3. 安装 APK，在 LSPosed 中启用模块并检查作用域。
4. 打开应用一次，然后**完整重启**设备。
5. 验证设置应用、SystemUI、桌面、锁屏和常用 Hook。

只有包名、签名一致且新 APK 的版本号不低于已安装版本时才能覆盖安装；其他情况请先备份再卸载。

覆盖安装后、完整重启前，旧 SystemUI 进程可能因热加载新模块产生一次性 Hook 失败记录；完整重启后不再复现，不属于正式启动故障。因此升级模块后必须完整重启设备，不能只重启桌面或 SystemUI。

## 构建

需要 JDK 17 与 Android SDK：

```powershell
.\gradlew.bat --no-daemon clean assembleRelease lintRelease lintVitalRelease
```

Release 构建启用 R8、资源压缩、zipalign 和 APK Signature Scheme v2。正式签名从仓库外的 `../keystore.properties` 读取，密钥与口令不得提交。

## 来源与许可

米客 A14 由 `tomthenpc` 独立维护，不代表原作者或参考项目。源码来自 [Mikanoshi/CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer)，Android 14 功能基线参考 [MonwF/customiuizer](https://github.com/MonwF/customiuizer) 的 `v24.10.12`。

项目继续以 [GPL-3.0](LICENSE) 发布。分发 APK 时必须提供对应源码、保留许可证与版权来源，并明确标注修改。详见 [NOTICE.md](NOTICE.md)。

[English](README_en.md) | [日本語](README_jp.md) | [Português (Brasil)](README_PT-BR.md) | **简体中文**
