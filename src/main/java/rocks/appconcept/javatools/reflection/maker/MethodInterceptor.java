package rocks.appconcept.javatools.reflection.maker;

import java.lang.reflect.InvocationHandler;

/**
 * @author yanimetaxas
 */
public class MethodInterceptor {

  private MethodInterceptor() {
  }

  public static <T> T interceptMethods(Class<T> type, InvocationHandler interceptor) throws Exception {
      type.getConstructor(); // throw if no public constructor
      String className = type + "$JavaTools";
      ClassLoader classLoader = type.getClassLoader();
      Class<?> aClass;
      try {
        aClass = classLoader.loadClass(className);
      } catch (ClassNotFoundException cnfe) {
        aClass = ClassLoaderInjector.injectClass(classLoader, className, SubclassMaker.makeSubclass(className, type));
      }
      return type.cast(aClass.getConstructor(InvocationHandler.class).newInstance(interceptor));
  }
}
