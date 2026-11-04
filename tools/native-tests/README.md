# Native folder regression tests

Run `python3 tools/native-tests/test_page_transaction.py` on Linux with a C++20 compiler. On Windows run the same command with Python; the runner uses Ubuntu WSL and its `c++` compiler. Tests compile the production C++ transaction implementation and execute Linux mmap/mprotect/mremap operations with failure injection. They do not contact a phone.

Optional recorded-page tests require `FOLDER_NATIVE_EVIDENCE` pointing to the external recording directory containing `libapp.so` and the live-page files consumed by `test_page_transaction.cpp`. No launcher binaries or recorded device memory are shipped here. Tests requiring the recording explicitly skip without it.

The parameter matrix covers rows 5/6/7/8, columns 3/4/5/6, gaps 5/6/7/8, and density 848/898/948/998 per mille. It checks published globals and the generated height/child/drag instructions, not execution of Dart assembly or visible phone behavior. Independent marker/layout tests compare untouched pages byte-for-byte; marker failure checks restoration of all recorded pages.

## Geometry review

The three matched calGridHeight MOV instructions supply an untagged row cap. Downstream code computes ceil(app count / columns), boxes the cap, and calls the minimum operation. Configured rows therefore represent a maximum, not a promise to show empty rows in short folders. Other stock viewport restrictions remain. The row hook, child window and drag window use the same configured count.

The setup hook only adopts the stock tagged 3-column pool value or its own configured value. Unexpected values from another owner are preserved. This means a competing column override can intentionally prevent this module from applying its requested columns.

The calItemSpacing early path explicitly zeros horizontal and vertical spacing and returns before the shared epilogue. Its stock flag gating remains intact. Configured positive vertical gap replaces the value on the regular spacing path only; it does not force extra gap when the launcher selects its zero-spacing path. The hook preserves horizontal spacing.

Density is a ratio in thousandths. 848 means 0.848, intentionally rounded from the prior accepted experiment's 189.5/223.5 = approximately 0.84787472. It is not bit-identical to that experiment. Native accepts 848..1000; the product UI may expose 85..100 percent in increments of ten per mille. This numeric rounding and wider parameter range still require actual-device UI validation.

Preference parser tests (JDK17+):

```sh
mkdir -p /tmp/pengeek-folder-tests
javac -d /tmp/pengeek-folder-tests app/src/main/java/name/monwf/customiuizer/mods/NativeFolderSettings.java tools/native-tests/NativeFolderSettingsTest.java
java -ea -cp /tmp/pengeek-folder-tests NativeFolderSettingsTest
```

These cover default-off behavior, parameter mapping, independent flags, immutable
snapshots, wrong types and rejected ranges. They do not emulate Android Binder.
