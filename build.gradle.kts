plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.versions)
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
