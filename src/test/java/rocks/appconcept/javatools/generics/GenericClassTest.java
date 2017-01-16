package rocks.appconcept.javatools.generics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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