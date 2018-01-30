package rocks.appconcept.javatools.reflection.maker;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import org.junit.After;
import org.junit.Test;
import rocks.appconcept.javatools.CoverageTool;
import rocks.appconcept.javatools.reflection.ReflectionUtils;

/**
 * @author yanimetaxas
 *
 * @since 03-Dec-17
 */
public class BeanMakerTest {

  @Test
  public void makeBean() throws Exception {

    HashMap<String, String> properties = new HashMap<>();
    properties.put("test1", "boolean");
    properties.put("test2", "byte");
    properties.put("test3", "char");
    properties.put("test4", "double");
    properties.put("test5", "float");
    properties.put("test6", "int");
    properties.put("test7", "long");
    properties.put("test8", "short");
    properties.put("test9", "String");

    HashMap<String, String> lists = new HashMap<>();
    lists.put("test1", "boolean");
    lists.put("test2", "byte");
    lists.put("test3", "char");
    lists.put("test4", "double");
    lists.put("test5", "float");
    lists.put("test6", "int");
    lists.put("test7", "long");
    lists.put("test8", "short");
    lists.put("test9", "String");

    byte[] beanBytes = BeanMaker.makeBean("TestBean", "Object", properties, lists);

    //System.out.println(new String(beanBytes, "UTF-8"));
    assertEquals(ReflectionUtils.getClassNameFromByteCode(beanBytes), "TestBean");

  }

  @After
  public void tearDown() throws Exception {
    CoverageTool.testPrivateConstructor(BeanMaker.class);
  }
}