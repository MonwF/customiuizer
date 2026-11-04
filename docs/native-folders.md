# HyperOS native folders (draft integration)

## Public-source baseline

Upstream main and release tag v26.10.31 both resolve to e76c9204e8d2ff3f79a21aeebeb91dfb47d6654a.
That tree still declares version25.09.25 and API101, while the release separately
ships Pengeek-26.10.31-test.apk described as HyperOS4-only with a new UI. This is
NOT the full source tree for that APK. No26.10.29 source branch/tag was found.
This draft targets the public tree only. Maintainer review must first decide
whether to transplant the isolated native core into the unpublished/newer tree
or publish that tree and rebase. It is not ready to merge into a current-release
source tree we have not seen. No version string is changed to imply otherwise.

## Behavior

An opt-in native path complements the existing Java Launcher hooks. It supports
3-6 columns (existing column preference), 5-8 maximum visible rows, extra vertical
spacing 0-20 logical units, and cell-height scale848-1000 per mille. Display,
child materialization, animation and drag candidate windows use the same row
setting. The grid uses its constructor's default clipping object to avoid an
extra row flashing during opening. Four cell-height paths share the scale so
closing animations retain their endpoint. Extra spacing respects the Launcher's
own early zero-spacing branch; short folders retain fewer than the maximum rows.

The independent Hide new-app blue dots switch suppresses new-install prefixes;
it leaves underlying application flags and labels intact.
Both feature flags default off. Nothing is installed before a valid snapshot.
A default-off snapshot is authoritative and does not start scanning the launcher.

## Configuration and lifecycle

HYOS-only processes may have no ART/Java module callback. Settings are therefore
not passed through a hypothetical Launcher Java entry. The existing system_server
module entry publishes a cached, immutable RemotePreferences snapshot. A private
transaction0x0050464c on android.content.IClipboard$Stub answers seven int32s:
ACK0x50464c31, schema1, flags(layout1/marker2), columns, rows, gap, density.
Native uses the existing clipboard Binder service, not clipboard contents.
There is no new service, socket, root command, KSU module or persistent daemon.

The native client resolves libbinder_ndk explicitly for isolated linker namespaces,
queries at most five times on a startup worker, validates the complete fixed-size
reply and parameter ranges, then configures once per process. A synchronous Binder
call is NOT guaranteed a strict timeout by the retry budget. Nothing does Binder
IPC in a rendering hook. The endpoint caches settings before handling transactions;
remote preference updates publish a new immutable snapshot outside the transaction.

Unrecognized requests continue to the original service with their parcel cursor
restored. Only the private synchronous request with the clipboard descriptor and
no payload is handled. UID/PID/proc checks narrow accidental callers but are NOT
cryptographic or PackageManager package authentication; cmdline can be spoofed.
The endpoint exposes only these nonsensitive settings and grants no mutation.

Android framework and com.miui.home scopes are required. Initial activation of the
new system_server hook requires a framework-process restart (normally a device
reboot). Subsequent setting changes require only restarting Launcher; this port
makes no native hot-reload promise. Disabling the feature applies on next Launcher
start. The user's existing standalone module/device was not replaced for this PR.

## Discovery and recovery

Native code is adapted from the locally user-accepted lab13 standalone implementation.
Named-profile data plus semantic instruction relationships locate sites; unknown
Build IDs are attempted rather than rejected by package-version allowlists. Bounds
and instruction-shape checks protect writes. Changed/unrecognized binaries fall back
without activating the contract. Existing page modifications and supported veneers
are preserved. Transactions restore only owned words on failure, and stop retrying
once writes have been attempted or rollback is incomplete.

Columns preserve unrelated competing pool values. This does not promise arbitrary
foreign hooks can all be composed. The module does not modify MiuiHome.apk or
hyos_spawner. No original Xiaomi binary or device memory dump is included.

## Evidence and remaining gate

Standalone implementation: Xiaomi nezha, OS4.0.5.0.XPACNXM, MiuiHome801028593,
libapp BuildID4f1bdaed7c97ae76de7d3ebc14f6a7b0. The user accepted six columns/eight
rows, compact spacing, opening/closing animation, marker suppression, and drag
ordering on2026-11-03. The standalone used ratio189.5/223.5; UI848 per mille is
an intentional approximation, not identical bytes/numbers.

The integration adds configurable parameters and a NEW settings transport.
That transport, system_server hook, SELinux access and complete Pengeek native
loading are NOT device-validated here. The PR must remain draft until a maintainer
or tester validates that chain with the supported framework, then tests folder
open, scrolling, reorder at the bottom, close/reopen and independent marker mode.
Compilation and host tests do not imply these runtime checks passed.

Tests: tools/native-tests/README.md. Production transaction code is tested with
failure injection. Recorded-image replay is optional external evidence; it skips
explicitly without FOLDER_NATIVE_EVIDENCE, rather than shipping private dumps.

## Transport references

The existing-service transport design was informed by these GPLv3 projects;
this port implements its own small fixed-schema bridge, without adding either
project as a dependency:

- WOMMO NativeConfigEndpoint.kt, commit28d270f76c3877c290e416f39dbc5c17f497ca62:
  https://github.com/YifePlayte/WOMMO/blob/28d270f76c3877c290e416f39dbc5c17f497ca62/app/src/main/java/com/yifeplayte/wommo/hook/hooks/singlepackage/system/NativeConfigEndpoint.kt
- NativeHookKit config.cpp, commit99484defc0da80b1680176b8edaf6d1676833263:
  https://github.com/YifePlayte/NativeHookKit/blob/99484defc0da80b1680176b8edaf6d1676833263/src/config.cpp

The native hook implementation and tests are contributed under this repository's
GPLv3 license. Configuration transport is proposed for review because public
main did not expose the maintainer's current native runtime integration; an
existing maintainer transport can replace this bridge without changing the
immutable FolderConfig boundary.

## Local build validation

On2026-11-04: Gradle8.14.4 :app:assembleDebug completed successfully using
JDK24, NDK29.0.14206865 and Build Tools36.0.0. APK inspection confirmed
META-INF/xposed/native_init.list and arm64-v8a/libpengeek_folder_native.so.
23 native transaction tests passed with external recorded evidence; the Java
settings parser tests passed. This build deliberately retains upstream25.09.25
version metadata; it is a test build of the public source, not a26.10.31 release.
No generated APK is published or installed as an official Pengeek release.
