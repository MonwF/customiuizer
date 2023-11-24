![logo](https://code.highspec.ru/customiuizer_promo.png)

## CustoMIUIzer14

**English** | [日本語](README_JP.md) | [Português (Brasil)](README_PT-BR.md) | [中文](README_ZH.md)

Customize MIUI to your liking

For MIUI 13 & 14 based on android 12 and 13

> Thanks for the awesome module from [Mikanoshi](https://code.highspec.ru/Mikanoshi/CustoMIUIzer)

### Translations
[![Crowdin](https://badges.crowdin.net/customiuizer14/localized.svg)](https://crowdin.com/project/customiuizer14)

### Download
* [Releases](https://github.com/MonwF/customiuizer/releases)
* [Test Releases](https://rz3kv5wa4g.jiandaoyun.com/dash/650e43a383027ec3225083e9)

### Backup functionality
Backups are stored in `/sdcard/Documents/CustoMIUIzer/`

### Donate
Via paypal
* [$5](https://paypal.me/tpsxj/5)
* [$10](https://paypal.me/tpsxj/10)
* [Other](https://paypal.me/tpsxj)

------

## Original Readme

### What is CustoMIUIzer? ###
CustoMIUIzer module contains a variety of mods that will provide additional functionality for your MIUI device. You have to have (Ed)Xposed Framework installed to use these mods. For more info about Xposed refer to the <a href="http://forum.xda-developers.com/xposed/xposed-installer-versions-changelog-t2714053" target="_blank">original thread</a> or <a href="https://github.com/ElderDrivers/EdXposed" target="_blank">github repo of EdXposed (for Android 8-10)</a>.

### Compatibility ###
Module was written, tested and is mainly intended for MIUI 10-12.5 on Android 9-10.<br>
Mods are not guaranteed to fully work on any other versions, especially lower MIUI versions.<br>
APK installation is limited to Android 7+.

### How to use CustoMIUIzer? ###
First you must have (Ed)Xposed Framework installed. After that enable CustoMIUIzer module in (Ed)Xposed Installer, set up mods you like and select 'Soft Reboot' from menu.

### Backup functionality ###
CustoMIUIzer supports settings backup and restore using local backup on `/sdcard` (find it in main window's menu).<br>
It also supports automatic Google cloud backups (Android Backup Service).

### Troubleshooting ###
You can send a detailed report with a description of your problem from CustoMIUIzer itself or create new issue on <a href="https://code.highspec.ru/Mikanoshi/CustoMIUIzer/issues">issue tracker</a>.
<br><br>
<u>Common poblems</u><br><br>

<i>Module and mods were activated, device rebooted, but none of the mods are working.</i>
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
<a href="https://repo.xposed.info/module/name.mikanoshi.customiuizer" target="_blank">Xposed Module Repository</a><br>
<a href="https://play.google.com/store/apps/details?id=name.mikanoshi.customiuizer" target="_blank">Google Play Store</a><br>
<a href="https://customiuizer.oneskyapp.com/admin/project/dashboard/project/335607" target="_blank">Localization</a>
