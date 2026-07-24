# CustoMIUIzer A14

**米客 A14** は、**HyperOS 1 / Android 14** 専用に独立して保守されているシステムカスタマイズモジュールです。[MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) を A14 機能の参考にしつつ、独自のパッケージ、バージョン、ビルド、検証工程を持ちます。

主な違いは **libxposed API 101 対応**と継続的な**コード／リソース最適化**です。再起動後の Hook の信頼性を優先しながら、ホットパス、スレッド、キャッシュ、リフレクション、例外境界を改善します。

> [!WARNING]
> Android 14（SDK 34）および `arm64-v8a` 専用です。Android 15/16 では有効にせず、同系統のモジュールを同時に有効化しないでください。

## 現在の状態

- 安定版：r14.3.1
- 前の安定版：r14.3.0
- パッケージ：`tv.withaibuild.customiuizer.r14`
- LSPosed 基準：[Vector v2.0-3046](https://github.com/JingMatrix/Vector/actions/runs/29805285935)、commit `9350c7c`
- リリース：[tomthenpc/customiuizer-a14](https://github.com/tomthenpc/customiuizer-a14/releases)

r14.3.1 は現在の安定版です。r14.3.0 をベースにロック画面充電情報の重複排除、lint クリーンアップ、依存関係の更新を含み、ビルド・署名・`assembleRelease` を通過しています。r14.3.0 は実機で完全再起動と Launcher/SystemUI の主要機能を確認済みであり、最終ログに本モジュール由来の例外、クラッシュ、ANR、プロセス停止はありません。

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
