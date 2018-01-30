package rocks.appconcept.javatools.reflection.maker;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rocks.appconcept.javatools.CoverageTool;
import rocks.appconcept.javatools.reflection.ReflectionUtils;

/**
 * @author yanimetaxas
 * @since 30-Jan-18
 */
public class ClassLoaderInjectorTest {

  @Test
  public void injectClass_AddFieldDynamically() throws Exception {
    ClassFile classFile = new ClassFile(
        ClassConstants.ACC_PUBLIC | ClassConstants.ACC_FINAL | ClassConstants.ACC_SUPER,
        dotToSlash("rocks.appconcept.javatools.reflection.maker.ClassFile"), dotToSlash("java.lang.Object"));

    classFile.addField(ClassConstants.ACC_PUBLIC, "testField", "I");

    ClassLoader classLoader = classFile.getClass().getClassLoader().getParent();
    Class aClass = ClassLoaderInjector.injectClass(classLoader,
        "rocks.appconcept.javatools.reflection.maker.ClassFile",
        classFile.toByteArray());

    Class<?> reloaded = classLoader.loadClass(aClass.getName());

    assertEquals(reloaded.getSimpleName(), "ClassFile");
    assertEquals(reloaded.getField("testField").getName(), "testField");
    assertEquals(reloaded.getField("testField").getType().getSimpleName(), "int");
  }

  @After
  public void tearDown() throws Exception {
    CoverageTool.testPrivateConstructor(ClassLoaderInjector.class);
  }

  private static String dotToSlash(String name) {
    return name.replace('.', '/');
  }
}