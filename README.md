# 米客 A14（CustoMIUIzer A14）

**米客 A14** 是面向 **HyperOS 1 / Android 14** 的独立维护版系统定制模块。项目以 [MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) 为 Android 14 功能参考，但采用自己的包名、版本线、构建与发布流程。

## 当前版本与推荐

| 项目 | 当前值 |
|---|---|
| 当前稳定版 | **r14.6.2** |
| 上一稳定版 | r14.5.0 |
| 应用名 | 米客 A14 |
| 包名 | `tv.withaibuild.customiuizer.r14` |
| 目标系统 | HyperOS 1 / Android 14（minSdk 34，仅 `arm64-v8a`） |
| 代码基线 | 基于 HyperOS 1 / Android 14（API 34）实机适配，参考 MonwF/customiuizer v24.10.12 |
| Hook 接口 | libxposed API 101 |
| LSPosed 基线 | [Vector v2.0-3046](https://github.com/JingMatrix/Vector/actions/runs/29805285935)，commit `9350c7c` |
| 发布页 | [tomthenpc/customiuizer-a14 Releases](https://github.com/tomthenpc/customiuizer-a14/releases) |

- **推荐日常使用的稳定版**：**r14.6.2**。它在 r14.6.1 热修复基础上清理了死代码、废弃 tag 和未使用权限；LSPosed 重启日志未出现模块相关 `AndroidRuntime`/`FATAL`/`am_crash`/`am_anr`，可覆盖安装。
- **r14.5.0** 仍是上一完整验证基线，如果你更看重经过多轮重启测试的保守版本，可以继续使用。
- **r14.6.0 与 r14.6.1 的 release/tag 已删除**：r14.6.0 因 `systemui` 启动崩溃被废弃，r14.6.1 已合并到 r14.6.2。

> [!WARNING]
> 仅支持 **Android 14（SDK 34）** 与 `arm64-v8a`。不要在 Android 15/16 上启用，也不要与上游版或其他同源分支同时启用，否则可能产生重复 Hook。

---

## 各版本全方位对比

下表覆盖从上游参考到 r14.6.2 的全部发行版，对比体积、加载速度、主要优化方向、稳定性与推荐度。APK 大小来自构建产物，加载时间来自同一设备 LSPosed/Vector 日志，详细样本见「模块加载实测」。

| 版本 | 状态 | APK 大小 | 相对上一版 | 模块加载中位 | 主要优化方向 | 稳定性 | 推荐度 |
|---|---|---:|---:|---:|---|---|---|
| [上游 v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) | 参考基线 | 2.78 MB | — | — | 上游 Android 14 功能参考，保留联网权限与支持资源 | 稳定 | 仅供参考 |
| [r14.0.0](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.0.0) | 初始版 | 2.90 MB | +116 KB | — | 建立独立版本线，引入类/方法/参数/资源/主题缓存 | 未完整验证 | 不推荐 |
| [r14.1.3](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.1.3) | 稳定版 | 2.89 MB | −15 KB | 19 ms | 资源瘦身最大：移除联网权限、非支持资源、限制图标缓存 | 验证无崩溃 | 推荐 |
| [r14.2.0](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.0) | 稳定版 | 2.89 MB | −85 B | 6 ms | 偏好设置/Hook/反射热路径优化，秒针调度 | 验证无崩溃 | 推荐 |
| [r14.2.4](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.4) | 稳定版 | 2.89 MB | 0 B | 22 ms | 反射缓存、无效 Hook/Receiver 去重 | 验证无崩溃 | 推荐 |
| [r14.2.7](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.7) | 稳定版 | 2.89 MB | 0 B | 6 ms | 自定义动作/手势 gate，未配置时零成本 | 验证无崩溃 | 推荐 |
| [r14.2.9](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.2.9) | 稳定版 | 2.89 MB | −4 B | 20 ms | 电池/状态栏绘制缓存，Matrix 对象复用 | 验证无崩溃 | 推荐 |
| [r14.3.1](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.3.1) | 稳定版 | 2.89 MB | 0 B | 15 ms | 锁屏充电去重、天气查询线程池化 | 验证无崩溃 | 推荐 |
| [r14.5.0](https://github.com/tomthenpc/customiuizer-a14/releases/tag/r14.5.0) | 稳定版 | 2.89 MB | +208 B | 57 ms | 包名迁移，资源 ID 查找缓存，生命周期加固 | 完整重启验证通过 | 上一稳定基线 |
| r14.6.2（本版） | **稳定版** | 2.90 MB | +13.8 KB | 参考 57 ms | 修复 r14.6.0 崩溃，清理死代码/权限/错误标签 | 无新增崩溃/ANR | **推荐日常版** |

> **关于“省电性”的说明**：
> r14.2.7 及以后通过“功能未开启时不注册 Hook/Receiver/Observer”、减少对象分配与线程池化来降低 CPU/内存/唤醒开销，对续航与流畅度有正面作用。但 APK 大小和加载速度**不能直接等同于省电收益**；要量化续航改善，必须在同设备、同功能开关、同使用场景下使用 **Perfetto / Batterystats** 长时间对照采样。

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
| 只求稳定、省电、不折腾 | **r14.6.2** | 基于 r14.6.1 热修复与包名迁移，清理死代码/权限/标签，LSPosed 重启日志无模块崩溃/ANR |
| 保守稳定基线 | **r14.5.0** | 多轮完整重启验证，无模块崩溃/ANR，架构与缓存都已落地 |
| 想跟随上一版验证版 | **r14.3.1** | 功能与稳定兼顾，WeatherDataController 线程池化明显 |
| 重度状态栏/电池绘制定制 | **r14.2.9** | BatteryIndicator 绘制热路径缓存，Matrix 复用 |
| 自定义动作/控制中心开关较多 | **r14.2.7** | 自定义动作 gate 与 Launcher gesture gate，未配置时不加载控制器 |
| 已安装 r14.6.0/14.6.1 并遇到问题 | **r14.6.2** | 修复 systemui 崩溃并清理发布标签，状态已更新为稳定版 |

---

## 模块加载实测

数据来自同一设备上的 **VectorModuleManager** 日志，统计 `Loading module` → `Loaded module ... successfully` 之间的时间差。样本量不大，且受开机阶段 CPU/IO 竞争影响，部分极值（如 643 ms、2676 ms）是系统整体负载造成的瞬时抖动，不代表模块本身慢。

### 按版本阶段汇总

| 版本阶段 | 包名 | 样本数 | 最小（ms） | 中位（ms） | 最大（ms） | 说明 |
|---|---|---:|---:|---:|---:|---|
| r14.0.0 | `name.monwf.customiuizer.r14` | 7 | 6 | 9 | 25 | 初始版本，模块加载已能在 10 ms 内完成 |
| r14.1.3 | `name.monwf.customiuizer.r14` | 3 | 8 | 19 | 25 | 资源瘦身和线程池治理后加载时间仍稳定 |
| r14.2.0 | `name.monwf.customiuizer.r14` | 4 | 5 | 6 | 33 | 热路径优化生效，中位降至 6 ms |
| r14.2.4 | `name.monwf.customiuizer.r14` | 1 | 22 | 22 | 22 | 样本少，参考意义有限 |
| r14.2.7 | `name.monwf.customiuizer.r14` | 3 | 6 | 6 | 24 | 自定义 gate 未增加加载开销 |
| r14.2.9 | `name.monwf.customiuizer.r14` | 14 | 6 | 20 | 99 | 电池/状态栏绘制缓存正常加载 |
| r14.3.1 | `name.monwf.customiuizer.r14` | — | — | 15（历史 2） | — | 本轮未采集到新样本，保留历史参考 |
| r14.5.0+ | `tv.withaibuild.customiuizer.r14` | 3 | 43 | 57 | 95 | 包名迁移后，资源缓存与生命周期加固正常 |
| r14.6.2 | `tv.withaibuild.customiuizer.r14` | 0 | — | 同阶段 57 | — | 本次重启日志未触发目标进程加载，无新增样本 |

### 关键结论

- **模块加载中位数在 6–57 ms 之间**，早期版本（r14.2.0、r14.2.7）能做到 6 ms 中位；包名迁移后的 r14.5.0+ 样本在 43–95 ms 中位 57 ms，受开机负载影响偏大，但仍属正常范围。
- **没有一次模块加载失败或崩溃**：所有 `Loaded module ... successfully` 均正常返回。
- 日志中同时出现的 `OnGlobalListenerError`、`ImageView`、`TransitionCallback`、`WindowElement` 等系统/其他模块 tag 异常，均不含 `tv.withaibuild.customiuizer` 或 `name.monwf.customiuizer` 调用栈，未追溯到 CustoMIUIzer 代码。
- r14.6.2 本轮日志虽无 `VectorModuleManager` 加载本模块的新样本，但也没有模块崩溃/ANR；建议后续再抓一次开启全部作用域后的启动日志，以补全 r14.6.2 的加载数据。

## 版本演进与静态对比

### 版本变化时间线

- **r14.0.0（2026-07-20）**：建立独立 A14 版本线，接入 libxposed API 101，加入类/方法/参数/资源/主题缓存；体积最大（2.9 MB），稳定性尚未完整验证。
- **r14.1.3（2026-07-23）**：资源瘦身最明显的一版：移除联网权限、非支持资源、限制图标缓存；修复 locale 与主线程 Handler 的静态引用问题；加载中位 19 ms。
- **r14.2.0（2026-07-23）**：偏好设置、Hook 参数、反射调用与秒针调度进入热路径优化；加载中位降至 6 ms。
- **r14.2.4（2026-07-23）**：合并 r14.2.1–2.3 的反射缓存与无效 Hook/Receiver 去重治理。
- **r14.2.7（2026-07-23）**：自定义动作与桌面手势增加“开关 gate”，未开启对应功能时不注册 Controller/Receiver，做到零后台开销。
- **r14.2.9（2026-07-23）**：计步器生命周期治理、电池指示器与状态栏图标绘制缓存，减少每帧对象分配。
- **r14.3.1（2026-07-24）**：锁屏充电提示去重、天气查询线程池化，避免每分钟新建线程。
- **r14.5.0（2026-07-24）**：源码包名统一迁移到 `tv.withaibuild.customiuizer`，资源 ID 查找加入热缓存，生命周期与 Handler 进一步加固；完整重启验证无崩溃。
- **r14.6.0（2026-07-24）**：尝试将部分系统设置键迁移到模块本地存储，并新增 PendingIntent 兼容、通知渠道与权限声明；但触发 SystemUI 启动早期崩溃，被废弃。
- **r14.6.1（2026-07-24）**：紧急回退本地存储迁移，恢复系统设置方式；LSPosed 日志无模块报错。
- **r14.6.2（2026-07-24）**：清理 r14.6.0/r14.6.1 的错误 release/tag，合并 changelog，移除死代码与未使用权限，重写 README 与版本对比；状态更新为稳定版。

### 静态对比要点

| 维度 | r14.0.0 | r14.1.3 | r14.2.0–r14.2.9 | r14.3.1 | r14.5.0 | r14.6.2 |
|---|---|---|---|---|---|---|
| APK 大小 | 最大 | 显著下降 | 持平 | 持平 | 略增 208 B | 再增 13.8 KB |
| 线程/Handler | 初始 | 主线程 Looper 修复 | 线程池/Observer 治理 | `WeatherDataController` 线程池 | 结构统一 | 无改动 |
| 反射/缓存 | 初始 | Locale/Handler 静态清理 | 热路径缓存与哨兵 | 充电/天气去重 | `getResId()` 热路径 | 清理死代码 |
| 运行时权限 | 较多 | 移除网络 | 稳定 | 稳定 | 稳定 | 移除 `POST_NOTIFICATIONS` |
| 稳定性 | 未验证 | 验证无崩溃 | 验证无崩溃 | 验证无崩溃 | 完整重启验证 | 无新增崩溃/ANR |

---

## 与参考版本的区别

| 维度 | 米客 A14 |
|---|---|
| 维护范围 | 固定面向 HyperOS 1 / Android 14，避免向未验证系统注册 Hook |
| 代码基线 | 基于 HyperOS 1 / Android 14（API 34）实机适配，以 MonwF/customiuizer v24.10.12 为功能参考 |
| Hook 架构 | libxposed API 101；按模块使用原生拦截器或稳定兼容层，不调用 Android 14 独占运行时接口 |
| 安装身份 | 独立 `applicationId` `tv.withaibuild.customiuizer.r14`，可与参考源码和发布历史区分 |
| 性能策略 | 优化高频 Hook、共享有界线程池、限制缓存、减少反射和重复计算 |
| 轻量化 | 移除下载、仓库、赞赏、内置网页与网络权限 |
| 发布策略 | 每版执行混淆构建、Lint、zipalign、签名和多轮 LSPosed 实机重启验证 |

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
