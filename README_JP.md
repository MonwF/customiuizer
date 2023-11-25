![logo](https://code.highspec.ru/customiuizer_promo.png)

## CustoMIUIzer14

[English](README.md) | **日本語** | [Português (Brasil)](README_PT-BR.md) | [中文](README_ZH.md)

MIUIを自分好みにカスタマイズ

Android 12/13ベースなMIUI 13/14に対応しています。

### ダウンロード
* [リリース](https://github.com/MonwF/customiuizer/releases)
* [テスト版リリース](https://rz3kv5wa4g.jiandaoyun.com/dash/650e43a383027ec3225083e9)

### バックアップ機能
バックアップは `/sdcard/Documents/CustoMIUIzer/` に保存されます。

### 寄付
PayPalから行えます
* [$5](https://paypal.me/tpsxj/5)
* [$10](https://paypal.me/tpsxj/10)
* [その他](https://paypal.me/tpsxj)

------

## オリジナルのReadme

### CustoMIUIzerとは? ###
CustoMIUIzerモジュールは、あなたのMIUIデバイスに様々な追加機能を提供するMODが含まれています。これらのModを使用するには(Ed)Xposed Frameworkをインストールする必要があります。Xposedについては、<a href="http://forum.xda-developers.com/xposed/xposed-installer-versions-changelog-t2714053" target="_blank">オリジナル版のスレッド</a>または、EdXposedの<a href="https://github.com/ElderDrivers/EdXposed" target="_blank">EdXposedのGitHubリポジトリ</a>をご参照ください。(Android 8-10用)

### 互換性 ###
このモジュールは、Android 9-10がベースのMIUI 10-12.5を対象に作成と動作確認をしています。<br>
Modは、他のバージョンや下位バージョンのMIUIで完全に動作する事は保証していません。<br>
APKのインストールはAndroid 7以降に限定されます。<br>

### CustoMIUIzerを使用するには? ###
始めに(Ed)Xposed Frameworkがインストールされている環境が必要になります。その後に(Ed)Xposed InstallerでCustoMIUIzerのモジュールを有効化し、好きなModを設定や選択を行なったら「ソフトリブート」を選択してください。

### バックアップ機能 ###
CustoMIUIzerは、SDカードまたは内部ストレージにローカルでバックアップと復元が行えます。(メインウィンドウ上のメニューに項目があります)<br>
その他にGoogleクラウドの自動バックアップ機能(Androidバックアップサービス)にも対応しています。

### トラブルシューティング ###
CustoMIUIzer上から問題の詳細を報告か、<a href="https://code.highspec.ru/Mikanoshi/CustoMIUIzer/issues">Issue Tracker</a>で新規のIssueを作成する事が可能です。
<br><br>
<u>よくある問題</u><br><br>

<i>モジュールとModを起動したが、デバイスの再起動後にどのModも一切動作をしていない。</i>
<hr>
(Ed)Xposed Installer > ログを開き、情報を確認します。<br>
"Loading modules from .../name.mikanoshi.customiuizer/..."と言う行を探します。<br>
もし、次の行に「File does not exist」が含まれている場合はCustoMIUIzerを再インストールをしてみると良いかもしれません。<br>
次の行が何か他の物(error, exceptionなど)を含んでいた場合は、CustoMIUIzerから詳細なレポートを送信してください。問題が解決されることを願っています:)
<br><br>
<i>CustoMIUIzerからレポートを送信し、返信を待っています。レポートは届いていますか?</i>
<hr>
連絡先を入力しないと絶対に返信は届きません。<br>
CustoMIUIzerのメイン画面 > 連絡先の情報にメールアドレス、ICQ、XDAまたは4PDAのニックネームを入力してください。
<br>
<a href="https://repo.xposed.info/module/name.mikanoshi.customiuizer" target="_blank">Xposedモジュールのリポジトリ</a><br>
<a href="https://play.google.com/store/apps/details?id=name.mikanoshi.customiuizer" target="_blank">Google Playストア</a><br>
<a href="https://customiuizer.oneskyapp.com/admin/project/dashboard/project/335607" target="_blank">ローカライズ</a>
