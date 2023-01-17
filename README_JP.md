![logo](https://code.highspec.ru/customiuizer_promo.png)

## CustoMIUIzer14 ([中文](./README_zh.md))
MIUIを自分好みにカスタマイズ

Android 12/13ベースなMIUI 13/14に対応しています。

### ダウンロード
* [リリース](https://github.com/MonwF/customiuizer/releases)
* LSPosedのリポジトリ
* [テスト版リリース](https://tpsx.lanzouv.com/b021ly4gj) パスワード: `miui`

### バックアップ機能
バックアップは「/sdcard/Documents/CustoMIUIzer/」に保存されます

### 寄付
PayPalから行えます
* [$5](https://paypal.me/tpsxj/5)
* [$10](https://paypal.me/tpsxj/10)
* [その他](https://paypal.me/tpsxj)

------

## オリジナルのReadme

### CustoMIUIzerとは? ###
CustoMIUIzerモジュールは、あなたのMIUIデバイスに様々な追加機能を提供するMODが含まれています。これらのModを使用するには(Ed)Xposed Frameworkをインストールする必要があります。Xposedについては、<a href="http://forum.xda-developers.com/xposed/xposed-installer-versions-changelog-t2714053" target="_blank">オリジナル版のスレッド</a>または、EdXposedの<a href="https://github.com/ElderDrivers/EdXposed" target="_blank">EdXposedのGitHubリポジトリ</a>をご参照ください。(Android 8ｰ10用)。

### 互換性 ###
このモジュールは、Android 9-10がベースのMIUI 10-12.5を対象に作成とテストしています。<br>
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

<i>モジュールとModを起動したが、デバイスの再起動後のどのModも動作がしていない。</i>
<hr>
Go to (Ed)Xposed Installer > Log and check information there.<br>
Find line "Loading modules from .../name.mikanoshi.customiuizer/...".<br>
If next line contains "File does not exist", then just reinstall CustoMIUIzer.<br>
If next line contains something else (error, exception, etc), then send detailed report from CustoMIUIzer and hope this problem can be fixed :)
<br><br>
<i>I sent a report from CustoMIUIzer and now waiting for response, where is it?</i>
<hr>
You will never get a response if you don't enter your contact info.<br>
Main CustoMIUIzer screen > Contact information. Write your e-mail, ICQ or XDA/4PDA nickname there.<br>
<br>
<a href="https://repo.xposed.info/module/name.mikanoshi.customiuizer" target="_blank">Xposedモジュールのリポジトリ</a><br>
<a href="https://play.google.com/store/apps/details?id=name.mikanoshi.customiuizer" target="_blank">Google Playストア</a><br>
<a href="https://customiuizer.oneskyapp.com/admin/project/dashboard/project/335607" target="_blank">ローカライズ</a>
