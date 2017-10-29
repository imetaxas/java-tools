package rocks.appconcept.javatools.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import junit.framework.TestCase;
import rocks.appconcept.javatools.CoverageTool;

/**
 * @author yanimetaxas
 */
@SuppressWarnings({"unused", "EqualsWhichDoesntCheckParameterClass"})
public class JSONTest extends TestCase {

  public static class BeanWithToJSON {

    public String member = "member";

    public String toJSON() {
      return "\"hello there\"";
    }
  }

  public static class BeanWithTypes {

    public String stringMember = "member";
    public float floatMember = 123.0f;
    public double doubleMember = 123.0f;
    public long longMember = 1234;
    public boolean booleanMember = true;

    @Override
    public boolean equals(Object o) {
      BeanWithTypes that = (BeanWithTypes) o;
      return Objects.equals(floatMember, that.floatMember) &&
          Objects.equals(doubleMember, that.doubleMember) &&
          Objects.equals(longMember, that.longMember) &&
          Objects.equals(booleanMember, that.booleanMember) &&
          Objects.equals(stringMember, that.stringMember);
    }
  }

  public static class BeanWithAggregate {

    public int intMember = 123;
    public BeanWithTypes inner = new BeanWithTypes();
    public BeanWithTypes inner2 = null;

    public boolean equals(Object o) {
      BeanWithAggregate that = (BeanWithAggregate) o;
      return Objects.equals(intMember, that.intMember) &&
          Objects.equals(inner, that.inner) &&
          Objects.equals(inner2, that.inner2);
    }
  }

  public static class BeanWithArray {

    public int[] intMember = {1, 2, 3, 4};
    public float[] floatMember = {1, 2, 3, 4};
    public boolean[] boolMember = {true, false};
    public BeanWithTypes[] inner = {new BeanWithTypes(), new BeanWithTypes()};
    public List<BeanWithTypes> innerList = Arrays.asList(new BeanWithTypes(), new BeanWithTypes());
    public List<List<List<String>>> innerList2 = Collections
        .singletonList(Collections.singletonList(Arrays.asList("apa", "bil")));

    public boolean equals(Object o) {
      BeanWithArray that = (BeanWithArray) o;
      return Arrays.equals(intMember, that.intMember) &&
          Arrays.equals(floatMember, that.floatMember) &&
          Arrays.equals(boolMember, that.boolMember) &&
          Arrays.equals(inner, that.inner) &&
          Objects.equals(innerList, that.innerList) &&
          Objects.equals(innerList2, that.innerList2);
    }
  }


  public static class BeanWithMap {

    public Map<String, String> stringMap = Collections.singletonMap("apa", "value");
    public Map<String, Boolean> enablement = Collections.singletonMap("apa", true);
    public Map<String, Float> values = Collections.singletonMap("apa", 123.0f);
    public Map<String, BeanWithArray> stringToBeans = Collections
        .singletonMap("prop", new BeanWithArray());

    public boolean equals(Object o) {
      BeanWithMap that = (BeanWithMap) o;
      return Objects.equals(stringMap, that.stringMap) &&
          Objects.equals(enablement, that.enablement) &&
          Objects.equals(values, that.values) &&
          Objects.equals(stringToBeans, that.stringToBeans);
    }
  }

  public void testStringify() throws Exception {
    assertEquals("toJSON methods should be called if present", "\"hello there\"",
        JSON.stringify(new BeanWithToJSON()));

    JSONObject parse = JSONObject.parse(JSON.stringify(new BeanWithTypes()));
    assertEquals("member", parse.get("stringMember").asString());
    assertEquals(123.0f, parse.get("floatMember").asFloat());
    assertEquals(1234l, (long) parse.get("longMember").asLong());
    assertEquals(123.0, parse.get("doubleMember").asDouble());
    assertEquals(true, parse.get("booleanMember").asBoolean());
  }

  public void testRoundTrips() throws Exception {
    assertEquals(new BeanWithAggregate(),
        JSON.parse(BeanWithAggregate.class, JSON.stringify(new BeanWithAggregate())));
    assertEquals(new BeanWithArray(),
        JSON.parse(BeanWithArray.class, JSON.stringify(new BeanWithArray())));
    assertEquals(new BeanWithMap(),
        JSON.parse(BeanWithMap.class, JSON.stringify(new BeanWithMap())));
  }


  public void testParseComplicated() throws Exception {
    String json = "{\"intMember\":[6,5,4],\"floatMember\":[],\"boolMember\":[true,false],\"inner\":[{\"stringMember\":\"member\",\"floatMember\":123.5,\"doubleMember\":123.1,\"longMember\":1234,\"booleanMember\":true},{\"stringMember\":\"member\",\"floatMember\":123.5,\"doubleMember\":123.1,\"longMember\":1234,\"booleanMember\":true}],\"innerList\":[{\"stringMember\":\"member\",\"floatMember\":123.5,\"doubleMember\":123.1,\"longMember\":1234,\"booleanMember\":true},{\"stringMember\":\"member\",\"floatMember\":123.5,\"doubleMember\":123.1,\"longMember\":1234,\"booleanMember\":true}],\"innerList2\":null}";
    BeanWithArray parse = JSON.parse(BeanWithArray.class, json);
    String stringify = JSON.stringify(parse);
    assertEquals(json, stringify);
  }

  public void testParseStringifyWithMap() throws Exception {

    BeanWithMap original = new BeanWithMap();

    String stringify = JSON.stringify(original);
    BeanWithMap parsed = JSON.parse(BeanWithMap.class, stringify);
    String stringify2 = JSON.stringify(parsed);
    assertEquals(stringify, stringify2);
  }

  public void testErrorHandling() throws Exception {

    assertEquals(null, JSON.parse(String.class, null));
    try {
      JSON.parse(String.class, "");
      fail("should have failed");
    } catch (JSON.JSONException ignored) {
    }

  }

  public static class HideStatic {

    public String x = "value";
    private static final String C1 = "C1";
    public static final String C2 = "C2";
    public String y = "u";
    private String z = "z";
  }

  public void testHidingStatic() throws Exception {
    HideStatic template = new HideStatic();
    template.x = template.y = template.z = "AAA";
    String stringify = JSON.stringify(template);
    JSONObject json = JSONObject.parse(stringify);
    assertTrue(json.get("x").isString());
    assertTrue(json.get("y").isString());
    assertTrue(json.get("z").isString());
    assertFalse(json.get("C1").isString());
    assertFalse(json.get("C2").isString());

    HideStatic roundtripped = JSON.parse(HideStatic.class, stringify);
    assertEquals(roundtripped.x, "AAA");
    assertEquals(roundtripped.y, "AAA");
    assertEquals(roundtripped.z, "AAA");
  }

  public void testStatic() throws Exception {
    CoverageTool.testPrivateConstructor(JSON.class);
  }

  private enum Color {
    RED(1), BLUE(2), GREEN(3);
    int num;

    Color(int i) {
      num = i;
    }
  }

  public static class BeanWithEnum {

    public Color color;
  }

  public void testWithEnum() throws Exception {

    {
      BeanWithEnum b = new BeanWithEnum();
      BeanWithEnum roundtrip = JSON.parse(BeanWithEnum.class, JSON.stringify(b));
      assertNull(roundtrip.color);
    }

    {
      BeanWithEnum b = new BeanWithEnum();
      b.color = Color.BLUE;
      String stringify = JSON.stringify(b);
      BeanWithEnum roundtrip = JSON.parse(BeanWithEnum.class, stringify);
      assertSame(Color.BLUE, roundtrip.color);
    }
  }
}
