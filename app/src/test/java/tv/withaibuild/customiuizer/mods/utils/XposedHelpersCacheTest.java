package tv.withaibuild.customiuizer.mods.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class XposedHelpersCacheTest {

    private static final ClassLoader LOADER = XposedHelpersCacheTest.class.getClassLoader();

    public static class Sample {
        public int publicField;
        public void doIt(String a) {}
        public void doIt(String a, int b) {}
        public void doIt(Object a) {}
    }

    @Test
    public void findClassReturnsCachedClass() {
        Class<?> first = XposedHelpers.findClass("java.lang.String", LOADER);
        Class<?> second = XposedHelpers.findClass("java.lang.String", LOADER);
        assertSame(first, second);
    }

    @Test
    public void findClassIfExistsCachesNegativeResult() {
        Class<?> first = XposedHelpers.findClassIfExists("does.not.Exist", LOADER);
        assertNull(first);
        Class<?> second = XposedHelpers.findClassIfExists("does.not.Exist", LOADER);
        assertNull(second);
    }

    @Test
    public void findFieldReturnsSameInstanceFromCache() {
        Field first = XposedHelpers.findField(Sample.class, "publicField");
        Field second = XposedHelpers.findField(Sample.class, "publicField");
        assertSame(first, second);
    }

    @Test
    public void findFieldIfExistsReturnsNullForMissingField() {
        assertNull(XposedHelpers.findFieldIfExists(Sample.class, "missingField"));
        // a second call should also be null and use the cached NOT_FOUND value
        assertNull(XposedHelpers.findFieldIfExists(Sample.class, "missingField"));
    }

    @Test
    public void findMethodExactReturnsSameInstanceFromCache() {
        Method first = XposedHelpers.findMethodExact(Sample.class, "doIt", String.class);
        Method second = XposedHelpers.findMethodExact(Sample.class, "doIt", String.class);
        assertSame(first, second);
    }

    @Test
    public void findMethodExactThrowsNoSuchMethodErrorAndCaches() {
        try {
            XposedHelpers.findMethodExact(Sample.class, "doIt", int.class);
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError expected) {
            // expected
        }
        try {
            XposedHelpers.findMethodExact(Sample.class, "doIt", int.class);
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError expected) {
            // cached NOT_FOUND should also throw
        }
    }

    @Test
    public void findMethodBestMatchFallsBackToCompatibleSignature() {
        Method method = XposedHelpers.findMethodBestMatch(Sample.class, "doIt", Object.class);
        assertNotNull(method);
        Method cached = XposedHelpers.findMethodBestMatch(Sample.class, "doIt", Object.class);
        assertSame(method, cached);
    }

    @Test
    public void additionalInstanceFieldStoresAndRemovesValues() {
        Object instance = new Object();
        Object value = new Object();

        assertNull(XposedHelpers.getAdditionalInstanceField(instance, "key"));
        assertNull(XposedHelpers.setAdditionalInstanceField(instance, "key", value));
        assertSame(value, XposedHelpers.getAdditionalInstanceField(instance, "key"));
        assertSame(value, XposedHelpers.removeAdditionalInstanceField(instance, "key"));
        assertNull(XposedHelpers.getAdditionalInstanceField(instance, "key"));
    }

    @Test
    public void additionalInstanceFieldIsIsolatedByInstance() {
        Object a = new Object();
        Object b = new Object();

        XposedHelpers.setAdditionalInstanceField(a, "key", "A");
        XposedHelpers.setAdditionalInstanceField(b, "key", "B");

        assertEquals("A", XposedHelpers.getAdditionalInstanceField(a, "key"));
        assertEquals("B", XposedHelpers.getAdditionalInstanceField(b, "key"));
    }

    @Test
    public void classCacheUsesClassLoaderIdentity() {
        ClassLoader sameLoader = XposedHelpersCacheTest.class.getClassLoader();
        Class<?> fromFirstLoader = XposedHelpers.findClassIfExists("java.lang.Integer", sameLoader);
        Class<?> fromSecondLoader = XposedHelpers.findClassIfExists("java.lang.Integer", sameLoader);
        assertSame(fromFirstLoader, fromSecondLoader);
        assertNotNull(fromSecondLoader);
    }
}
