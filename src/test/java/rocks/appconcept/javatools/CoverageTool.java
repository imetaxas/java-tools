package rocks.appconcept.javatools;

import org.junit.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * @author yanimetaxas
 */
public class CoverageTool {

    public static void callPrivateConstructor(Class<?> c) {
        Constructor<?>[] declaredConstructors = c.getDeclaredConstructors();
        Assert.assertEquals("Class does has have 1 declared constructor", 1, declaredConstructors.length);
        Constructor<?> declaredConstructor = declaredConstructors[0];
        Assert.assertEquals("Constructor should be private", Modifier.PRIVATE, declaredConstructor.getModifiers() | Modifier.PRIVATE);
        try {
            declaredConstructor.setAccessible(true);
            declaredConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }
}
