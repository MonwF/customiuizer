## 米客(r14)

> [!注意]
> 本项目是基于 [MonwF/customiuizer](https://github.com/MonwF/customiuizer) 的**非官方优化 fork**，沿用 **GPL-3.0** 协议。
> 仅供个人学习交流与性能优化参考，原始作品版权归 **MonwF** 与 **Mikanoshi** 所有。
> 仅兼容：**HyperOS 1 / Android 14 / libxposed API 101**。
> 本 fork 使用 **libxposed API 101 原生 `XposedInterface.Hooker.intercept(Chain)`** 调度，应可配合 **LSPosed 2.0** 与 **Vector** 等支持 API 101 的框架使用；目前仅在 LSPosed + HyperOS 1 A14 实机测试。
> 包名已改为 `name.monwf.customiuizer.r14`，应用名为 **米客(r14) / Pengeek(r14)**。
> **安装本 fork 前请先在米客中备份设置，然后卸载官方原版并安装本 fork；两个模块不能同时启用，否则会导致冲突。**
> 版本规划：**r14.0.\*** 维持 API-100 风格回调；**r14.1.\*** 将重写为 API-101 风格。
> 完整优化记录与发布说明见 [CHANGELOG.md](CHANGELOG.md)。

[English](README_en.md) | [日本語](README_jp.md) | [Português (Brasil)](README_PT-BR.md) | **中文**

客制化你的 HyperOS

仅兼容基于 `Android 14` 的 `HyperOS`。

> 感谢`Mikanoshi`的 [CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer) 模块

### 过时版本
* [MIUI 14 下载](https://github.com/MonwF/customiuizer/releases/tag/v23.11.26)
* [MIUI 13 下载](https://github.com/MonwF/customiuizer/releases/tag/v23.08.26)

### 上游与发布

* 上游项目：[MonwF/customiuizer](https://github.com/MonwF/customiuizer)
* 本 fork 发布说明：[CHANGELOG.md](CHANGELOG.md)
* 协议：GPL-3.0（与上游一致）

### 测试设备

| 设备 | HyperOS | Android | SoC | 内存 | 说明 |
|------|---------|---------|-----|------|------|
| 小米 13 (2211133G) | 1.0.7.0.UMCTWXM | 14 (UKQ1.230804.001) | Snapdragon 8 Gen 2（最高 3.19 GHz） | 12 GB | 主要测试机；r3–r14 均正常重启并加载。 |

* 基带版本：`MPSS.DE.3.0.c1-GLB-Oct 17 2024-04:43:46`
* 内核版本：`5.15.123-android13-8-00008-g3ca6a2912c7e-ab11087001`

### 主要功能
* 双排状态栏
* 信任蓝牙和Wi-Fi禁止锁屏
* 查看已保存Wi-Fi密码
* 自动亮度范围限制
* 双排信号
* 状态栏显示电池温度和电池
* 跳过10s安全警告
* 音乐可视化
* 独立通知音量
* 专辑封面设置为壁纸
* 状态栏显秒与图标隐藏
* 彩色电池条
* 使用导航栏同时启用返回手势
* 锁屏打开手电筒
* 通知
  * 通知重要性设置
  * 自动展开
  * 小窗打开通知
  * 直接打开频道设置
* 浮窗记住打开状态和位置、移除黑名单（含分屏）
* 扩展电源菜单
* 允许直接更新系统应用
* 导航栏手势与自定义按钮
* 安装或升级app时显示更多信息
* 允许限制系统app使用网络

------

> 本 fork 仅供学习交流，原始项目打赏通道请参见 [MonwF/customiuizer](https://github.com/MonwF/customiuizer)。
