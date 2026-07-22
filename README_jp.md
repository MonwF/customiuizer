# CustoMIUIzer A14

**米客 A14** は、**HyperOS 1 / Android 14** 専用に独立して保守されているシステムカスタマイズモジュールです。libxposed API 101 を使用し、安定性、低負荷、安全なロールバックを優先します。

> [!WARNING]
> Android 14（SDK 34）および `arm64-v8a` 専用です。Android 15/16 では有効にせず、同系統のモジュールを同時に有効化しないでください。

## 現在の状態

- 開発版：r14.1.3（実機確認待ち）
- 安定版：r14.1.2
- パッケージ：`name.monwf.customiuizer.r14`
- LSPosed 基準：[Vector v2.0-3046](https://github.com/JingMatrix/Vector/actions/runs/29805285935)、commit `9350c7c`
- リリース：[tomthenpc/customiuizer-a14](https://github.com/tomthenpc/customiuizer-a14/releases)

修正版 r14.1.3 は、実機確認が完了するまで既存の GitHub プレリリースを置き換えません。

## 主な変更

- Launcher と SystemUI の Hook に対する R8 安全な `after` コールバック検出
- Xposed コールバックと通常のアプリ起動経路の分離
- ダウンロード、リポジトリ、寄付、内蔵 Web ページ、ネットワーク権限の削除
- アプリアイコン用スレッドプールとキャッシュの上限設定
- オーディオビジュアライザー処理とメインスレッドの画像比較を削減

対応状況は Xiaomi のシステムアプリおよび ROM のバージョンに依存します。詳細は [CHANGELOG.md](CHANGELOG.md) を参照してください。

## インストール

1. 現在のバージョンで設定をバックアップします。
2. 公式版や他の派生版を削除し、複数の同系モジュールを同時に有効化しないでください。
3. APK をインストールし、LSPosed でモジュールとスコープを確認します。
4. アプリを一度開いてから、端末を完全に再起動します。
5. 設定画面、SystemUI、ランチャー、ロック画面、常用機能を確認します。

## 出典とライセンス

本プロジェクトは独立して保守される派生版で、上流作者による公式版ではありません。[Mikanoshi/CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer) と [MonwF/customiuizer](https://github.com/MonwF/customiuizer) の Android 14 向け作業を基にしています。

[GPL-3.0](LICENSE) に基づいて配布します。詳細は [NOTICE.md](NOTICE.md) を参照してください。

[English](README_en.md) | **日本語** | [Português (Brasil)](README_PT-BR.md) | [简体中文](README.md)
