package tv.withaibuild.customiuizer.mods.utils

import org.junit.Assert.*
import org.junit.Test

class XposedHelpersCacheTest {

    private val LOADER = XposedHelpersCacheTest::class.java.classLoader

    class Sample {
        var publicField: Int = 0
        fun doIt(a: String) {}
        fun doIt(a: String, b: Int) {}
        fun doIt(a: Any) {}
    }

    @Test
    fun findClassReturnsCachedClass() {
        val first = XposedHelpers.findClass("java.lang.String", LOADER)
        val second = XposedHelpers.findClass("java.lang.String", LOADER)
        assertSame(first, second)
    }

    @Test
    fun findClassIfExistsCachesNegativeResult() {
        val first = XposedHelpers.findClassIfExists("does.not.Exist", LOADER)
        assertNull(first)
        val second = XposedHelpers.findClassIfExists("does.not.Exist", LOADER)
        assertNull(second)
    }

    @Test
    fun findFieldReturnsSameInstanceFromCache() {
        val first = XposedHelpers.findField(Sample::class.java, "publicField")
        val second = XposedHelpers.findField(Sample::class.java, "publicField")
        assertSame(first, second)
    }

    @Test
    fun findFieldIfExistsReturnsNullForMissingField() {
        assertNull(XposedHelpers.findFieldIfExists(Sample::class.java, "missingField"))
        // a second call should also be null and use the cached NOT_FOUND value
        assertNull(XposedHelpers.findFieldIfExists(Sample::class.java, "missingField"))
    }

    @Test
    fun findMethodExactReturnsSameInstanceFromCache() {
        val first = XposedHelpers.findMethodExact(Sample::class.java, "doIt", String::class.java)
        val second = XposedHelpers.findMethodExact(Sample::class.java, "doIt", String::class.java)
        assertSame(first, second)
    }

    @Test
    fun findMethodExactThrowsNoSuchMethodErrorAndCaches() {
        try {
            XposedHelpers.findMethodExact(Sample::class.java, "doIt", Int::class.javaPrimitiveType)
            fail("expected NoSuchMethodError")
        } catch (expected: NoSuchMethodError) {
            // expected
        }
        try {
            XposedHelpers.findMethodExact(Sample::class.java, "doIt", Int::class.javaPrimitiveType)
            fail("expected NoSuchMethodError")
        } catch (expected: NoSuchMethodError) {
            // cached NOT_FOUND should also throw
        }
    }

    @Test
    fun findMethodBestMatchFallsBackToCompatibleSignature() {
        val method = XposedHelpers.findMethodBestMatch(Sample::class.java, "doIt", Any::class.java)
        assertNotNull(method)
        val cached = XposedHelpers.findMethodBestMatch(Sample::class.java, "doIt", Any::class.java)
        assertSame(method, cached)
    }

    @Test
    fun additionalInstanceFieldStoresAndRemovesValues() {
        val instance = Any()
        val value = Any()

        assertNull(XposedHelpers.getAdditionalInstanceField(instance, "key"))
        assertNull(XposedHelpers.setAdditionalInstanceField(instance, "key", value))
        assertSame(value, XposedHelpers.getAdditionalInstanceField(instance, "key"))
        assertSame(value, XposedHelpers.removeAdditionalInstanceField(instance, "key"))
        assertNull(XposedHelpers.getAdditionalInstanceField(instance, "key"))
    }

    @Test
    fun additionalInstanceFieldIsIsolatedByInstance() {
        val a = Any()
        val b = Any()

        XposedHelpers.setAdditionalInstanceField(a, "key", "A")
        XposedHelpers.setAdditionalInstanceField(b, "key", "B")

        assertEquals("A", XposedHelpers.getAdditionalInstanceField(a, "key"))
        assertEquals("B", XposedHelpers.getAdditionalInstanceField(b, "key"))
    }

    @Test
    fun classCacheUsesClassLoaderIdentity() {
        val sameLoader = XposedHelpersCacheTest::class.java.classLoader
        val fromFirstLoader = XposedHelpers.findClassIfExists("java.lang.Integer", sameLoader)
        val fromSecondLoader = XposedHelpers.findClassIfExists("java.lang.Integer", sameLoader)
        assertSame(fromFirstLoader, fromSecondLoader)
        assertNotNull(fromSecondLoader)
    }

    @Test
    fun findConstructorExactReturnsSameInstanceFromCache() {
        val first = XposedHelpers.findConstructorExact(Sample::class.java, *emptyArray<Class<*>>())
        val second = XposedHelpers.findConstructorExact(Sample::class.java, *emptyArray<Class<*>>())
        assertSame(first, second)
    }

    @Test
    fun findConstructorBestMatchReturnsCompatibleConstructor() {
        val ctor = XposedHelpers.findConstructorBestMatch(String::class.java, *arrayOf<Class<*>>(ByteArray::class.java))
        assertNotNull(ctor)
        val cached = XposedHelpers.findConstructorBestMatch(String::class.java, *arrayOf<Class<*>>(ByteArray::class.java))
        assertSame(ctor, cached)
    }
}
