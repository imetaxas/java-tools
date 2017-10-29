package rocks.appconcept.javatools.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import rocks.appconcept.javatools.CoverageTool;
import rocks.appconcept.javatools.reflection.ReflectionUtils;

/**
 * Created by imeta on 22-Jan-17.
 */
public class ReflectionUtilsTest {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Inherited
  public @interface ControllerClass {

  }

  @ControllerClass
  static class Foo {

  }

  static class Bar extends Foo {

  }

  @ControllerClass
  static interface Baz {

  }

  static interface Qux extends Baz {

  }

  static class Beta implements Qux {

  }

  static class Alpha extends Beta {

  }

  static interface Blart {

  }

  static class Bogus implements Blart {

  }

  public interface AAA {

  }

  public interface BBB extends AAA {

  }

  public class CCC implements BBB, AAA {

  }

  private class Parent {

    private String foo;
  }

  private class Child extends Parent {

    private String foo;
  }

  @Test
  public void testClassAnnotation() {
    Annotation a = ReflectionUtils.getClassAnnotation(Bar.class, ControllerClass.class);
    assertNotNull(a);
    assertTrue(a instanceof ControllerClass);

    a = ReflectionUtils.getClassAnnotation(Alpha.class, ControllerClass.class);
    assertNotNull(a);
    assertTrue(a instanceof ControllerClass);

    a = ReflectionUtils.getClassAnnotation(Bogus.class, ControllerClass.class);
    assertNull(a);

    a = ReflectionUtils.getClassAnnotation(CCC.class, ControllerClass.class);
    assertNull(a);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface ControllerMethod {

    String allow();
  }

  static class Foo1 {

    @ControllerMethod(allow = "false")
    public void yelp() {
    }
  }

  static class Bar1 extends Foo1 {

  }

  static interface Baz1 {

    @ControllerMethod(allow = "false")
    void yelp();
  }

  static interface Qux1 extends Baz1 {

  }

  static class Beta1 implements Qux1 {

    public void yelp() {
    }
  }

  static class Alpha1 extends Beta1 {

  }

  static interface Blart1 {

    void yelp();
  }

  static class Bogus1 implements Blart1 {

    public void yelp() {
    }
  }

  @Test
  public void testMethodAnnotation() throws Exception {
    Method m = ReflectionUtils.getMethod(Bar1.class, "yelp");
    Annotation a = ReflectionUtils.getMethodAnnotation(m, ControllerMethod.class);
    assertNotNull(a);
    assertTrue(a instanceof ControllerMethod);
    assertEquals("false", ((ControllerMethod) a).allow());

    m = ReflectionUtils.getMethod(Alpha1.class, "yelp");
    a = ReflectionUtils.getMethodAnnotation(m, ControllerMethod.class);
    assertNotNull(a);
    assertTrue(a instanceof ControllerMethod);

    m = ReflectionUtils.getMethod(Bogus1.class, "yelp");
    a = ReflectionUtils.getMethodAnnotation(m, ControllerMethod.class);
    assertNull(a);
  }

  @Test(expected = ThreadDeath.class)
  public void testGetDeclaredFields() throws Exception {
    Class c = Parent.class;

    Field f = c.getDeclaredField("foo");

    Collection<Field> fields = mock(Collection.class);
    when(fields.add(f)).thenThrow(new ThreadDeath());
    ReflectionUtils.getDeclaredFields(Parent.class, fields);
  }

  @Test
  public void testDeepDeclaredFields() throws Exception {
    Calendar c = Calendar.getInstance();
    Collection<Field> fields = ReflectionUtils.getDeepDeclaredFields(c.getClass());
    assertTrue(fields.size() > 0);

    boolean miss = true;
    boolean found = false;
    for (Field field : fields) {
      if ("firstDayOfWeek".equals(field.getName())) {
        found = true;
        break;
      }

      if ("blart".equals(field.getName())) {
        miss = false;
      }
    }

    assertTrue(found);
    assertTrue(miss);
  }

  @Test
  public void testDeepDeclaredFieldMap() throws Exception {
    Calendar c = Calendar.getInstance();
    Map<String, Field> fields = ReflectionUtils.getDeepDeclaredFieldMap(c.getClass());
    assertTrue(fields.size() > 0);
    assertTrue(fields.containsKey("firstDayOfWeek"));
    assertFalse(fields.containsKey("blart"));

    Map<String, Field> test2 = ReflectionUtils.getDeepDeclaredFieldMap(Child.class);
    assertEquals(2, test2.size());
    assertTrue(test2.containsKey(
        "rocks.appconcept.javatools.reflection.ReflectionUtilsTest$Parent.foo"));
    assertFalse(test2.containsKey(
        "rocks.appconcept.javatools.reflection.ReflectionUtilsTest$Child.foo"));
  }

  @Test
  public void testGetClassName() throws Exception {
    assertEquals("null", ReflectionUtils.getClassName((Object) null));
    assertEquals("java.lang.String", ReflectionUtils.getClassName("item"));
  }

  @Test
  public void testGetClassAnnotationsWithNull() throws Exception {
    assertNull(ReflectionUtils.getClassAnnotation(null, null));
  }

  @After
  public void tearDown() throws Exception {
    CoverageTool.testPrivateConstructor(ReflectionUtils.class);
  }
}