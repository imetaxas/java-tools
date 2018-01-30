package rocks.appconcept.javatools.generics;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
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

  @Test(expected = RuntimeException.class)
  public void getType_WhenMissingTypeParameter() throws Exception {
    TypeReference typeReference = new TypeReference(){};

    typeReference.getType();
  }

  @Test
  public void getType_OfAnInstance() throws Exception {

    ExampleGenericClass<Integer> exampleInteger = new ExampleGenericClass<Integer>(){};
    ExampleGenericClass<String> exampleString = new ExampleGenericClass<String>(){};

    assertEquals(exampleInteger.getType(), Integer.class);
    assertEquals(exampleString.getType(), String.class);
  }

  @Test(expected = RuntimeException.class)
  public void getType_OfAnInstance_WhenMissingTypeParameter() throws Exception {
    ExampleGenericClass exampleInteger = new ExampleGenericClass(){};
    exampleInteger.getType();
  }

  @Test
  public void getType_OfATypeThatHasGenericType() throws Exception {

    List<String> stringList = new TypeReference<ArrayList<String>>() {}.newInstance();
    List genericList = new TypeReference<ArrayList>() {}.newInstance();

    assertThat(Collections.emptyList(), is(stringList));
    assertThat(Collections.emptyList(), is(genericList));
  }
}

class ExampleGenericClass<T> {
  <T> Type getType(){
    TypeReference<T> typeReference = new TypeReference<T>(getClass()){};
    return typeReference.getType();
  }
}