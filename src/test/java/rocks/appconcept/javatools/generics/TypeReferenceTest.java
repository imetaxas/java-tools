package rocks.appconcept.javatools.generics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Created by imeta on 11-Feb-17.
 */
public class TypeReferenceTest {

  @Test
  public void getType() throws Exception {

    TypeReference<String> typeReference = new TypeReference<String>(){};

    assertEquals(typeReference.getType(), String.class);
  }

  @Test
  public void getType_OfAnInstance() throws Exception {

    ExampleGenericClass<Integer> exampleInteger = new ExampleGenericClass<Integer>(){};
    ExampleGenericClass<String> exampleString = new ExampleGenericClass<String>(){};

    assertEquals(exampleInteger.getType(), Integer.class);
    assertEquals(exampleString.getType(), String.class);
  }

  @Test
  public void getType_OfATypeThatHasGenericType() throws Exception {

    List<String> stringList = new TypeReference<ArrayList<String>>() {}.newInstance();
    List genericList = new TypeReference<ArrayList>() {}.newInstance();

    assertNotNull(stringList);
    assertNotNull(genericList);
  }
}

class ExampleGenericClass<T> {
  <T> Type getType(){
    TypeReference<T> typeReference = new TypeReference<T>(getClass()){};
    return typeReference.getType();
  }
}