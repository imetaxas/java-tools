package rocks.appconcept.javatools.reflection.maker;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

/**
 * @author yanimetaxas
 */
public class ClassLoaderInjector {

  private static final ProtectionDomain protectionDomain;
  private static Method defineClass;

  static {
    protectionDomain = (ProtectionDomain) AccessController.doPrivileged((PrivilegedAction) MethodInterceptor.class::getProtectionDomain);
    AccessController.doPrivileged((PrivilegedAction) () -> {
      try {
        Class<?> loader = Class.forName("java.lang.ClassLoader");
        defineClass = loader.getDeclaredMethod("defineClass",
                String.class,
                byte[].class,
                Integer.TYPE,
                Integer.TYPE,
            ProtectionDomain.class);

        defineClass.setAccessible(true);
      } catch (ClassNotFoundException | NoSuchMethodException e) {
        throw new RuntimeException("Unable to define classes");
      }
      return null;
    });
  }

  private ClassLoaderInjector() {
  }

  public static Class<?> injectClass(ClassLoader loader, String className, byte[] classFile) throws Exception {
    Class<?> aClass = (Class) defineClass.invoke(loader, className, classFile, 0, classFile.length, protectionDomain);
    Class.forName(className, true, loader); // run initializers
    return aClass;
  }

}
