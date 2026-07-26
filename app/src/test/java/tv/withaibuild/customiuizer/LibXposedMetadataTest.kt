package tv.withaibuild.customiuizer

import java.util.Properties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LibXposedMetadataTest {

    @Test
    fun modulePropertiesDeclareApi101To102Compatibility() {
        val properties = Properties()
        javaClass.classLoader
            ?.getResourceAsStream("META-INF/xposed/module.prop")
            .use { input ->
                requireNotNull(input) { "META-INF/xposed/module.prop is missing from the test classpath" }
                properties.load(input)
            }

        assertEquals("101", properties.getProperty("minApiVersion"))
        assertEquals("102", properties.getProperty("targetApiVersion"))
        assertEquals("false", properties.getProperty("staticScope"))
        assertNull(properties.getProperty("autoHotReload"))
    }

    @Test
    fun xposedEntryDoesNotUseLegacyApiNamespace() {
        val entry = javaClass.classLoader
            ?.getResourceAsStream("META-INF/xposed/java_init.list")
            .use { input ->
                requireNotNull(input) { "META-INF/xposed/java_init.list is missing from the test classpath" }
                input.bufferedReader().readText().trim()
            }

        assertEquals("tv.withaibuild.customiuizer.MainModule", entry)
        assertFalse(entry.startsWith("de.robv.android.xposed."))
    }
}
