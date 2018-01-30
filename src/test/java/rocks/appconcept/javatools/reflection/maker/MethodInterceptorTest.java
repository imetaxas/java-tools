package rocks.appconcept.javatools.reflection.maker;

import static org.hamcrest.CoreMatchers.any;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import rocks.appconcept.javatools.CoverageTool;
import static org.junit.Assert.assertEquals;
/**
 * @author yanimetaxas
 * @since 30-Jan-18
 */
public class MethodInterceptorTest {

  @Test
  public void interceptMethods() throws Exception {
    TestClass testClass = new TestClass();
    testClass.setParameter("beforeMethodCall");
    testClass = MethodInterceptor.interceptMethods(TestClass.class, new MyInvocationHandler(testClass));

    assertEquals(testClass.getParameter(), "beforeMethodCall");
    assertEquals(testClass.getParameter(), "afterMethodCall");
  }

  @Test(expected = NoSuchMethodException.class)
  public void interceptMethods_WhenThereIsNoPublicConstructor() throws Exception {
    ClassFile classFile = new ClassFile(
        ClassConstants.ACC_PUBLIC | ClassConstants.ACC_FINAL | ClassConstants.ACC_SUPER,
        dotToSlash("rocks.appconcept.javatools.reflection.maker.ClassFile"), dotToSlash("java.lang.Object"));

    MethodInterceptor.interceptMethods(ClassFile.class, new MyInvocationHandler(classFile));
  }

  private static String dotToSlash(String name) {
    return name.replace('.', '/');
  }

  @After
  public void tearDown() throws Exception {
    CoverageTool.testPrivateConstructor(MethodInterceptor.class);
  }
}

class MyInvocationHandler implements InvocationHandler {

  private TestClass testClass;
  private ClassFile classFile;

  public MyInvocationHandler(TestClass testClass) {
    this.testClass = testClass;
  }

  public MyInvocationHandler(ClassFile classFile) {
    this.classFile = classFile;
  }

  @Override
  public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
    System.out.println("before method call : " + method.getName());
    Object result = method.invoke(testClass, args);
    System.out.println("after method call : " + method.getName());

    testClass.setParameter("afterMethodCall");

    return result;
  }
}