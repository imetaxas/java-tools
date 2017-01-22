package rocks.appconcept.javatools.generics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GenericClassTest {

  @Test
  public void getTypeInteger() throws Exception {
    GenericClass<Integer> genericClass = new GenericClass<>(Integer.class);

    assertEquals(genericClass.getType(), Integer.class);
  }

  @Test
  public void getTypeString() throws Exception {
    GenericClass<String> genericClass = new GenericClass<>(String.class);

    assertEquals(genericClass.getType(), String.class);
  }
}