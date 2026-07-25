package tv.withaibuild.customiuizer.utils

class ModData {

    enum class ModCat {
        pref_key_system,
        pref_key_launcher,
        pref_key_controls,
        pref_key_various
    }

    @JvmField
    var title: String = ""

    @JvmField
    var breadcrumbs: String = ""

    @JvmField
    var key: String = ""

    @JvmField
    var cat: ModCat = ModCat.pref_key_system

    @JvmField
    var sub: String = ""

    @JvmField
    var order: Int = 0
}
