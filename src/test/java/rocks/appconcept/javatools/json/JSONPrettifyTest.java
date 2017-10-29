package rocks.appconcept.javatools.json;

import junit.framework.TestCase;
import rocks.appconcept.javatools.CoverageTool;

/**
 * @author yanimetaxas
 */
public class JSONPrettifyTest extends TestCase {

  public void testBasicOperation() throws Exception {

    CoverageTool.testPrivateConstructor(JSONPrettify.class);

    String prettify = JSONPrettify.prettify("[{\"name\":\"yani\"},{\"property\":true}]");
    assertEquals("[\n" +
        "  {\n" +
        "    \"name\": \"yani\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"property\": true\n" +
        "  }\n" +
        "]", prettify);

    String x = JSONPrettify.prettify(
        "[{\"name\":\"\u0600d®åäöa\\\"ni\u200cel\",\"x\":{\"a\":false,\"apa\":1}},{\"property\":[true, false, [1,2,{\"a\":[1,2,3]}]]}]");
    assertEquals("[\n" +
        "  {\n" +
        "    \"name\": \"\\u0600d®åäöa\\\"ni\\u200cel\",\n" +
        "    \"x\": {\n" +
        "      \"a\": false,\n" +
        "      \"apa\": 1.0\n" +
        "    }\n" +
        "  },\n" +
        "  {\n" +
        "    \"property\": [\n" +
        "      true,\n" +
        "      false,\n" +
        "      [\n" +
        "        1.0,\n" +
        "        2.0,\n" +
        "        {\n" +
        "          \"a\": [\n" +
        "            1.0,\n" +
        "            2.0,\n" +
        "            3.0\n" +
        "          ]\n" +
        "        }\n" +
        "      ]\n" +
        "    ]\n" +
        "  }\n" +
        "]", x);

    assertEquals("[\n" +
        "\t[\n" +
        "\t\tnull\n" +
        "\t],\n" +
        "\t[]\n" +
        "]", JSONPrettify.prettify("[[null],[]]", "\t"));

    assertEquals("{apa", JSONPrettify.prettify("{apa"));

  }

  public void testInvalidDoubles() throws Exception {
    JSONObject parse = JSONObject.parse("[1.234]");
    parse.set(0, Double.NaN);
    String prettify = JSONPrettify.prettify(parse, " ");
    assertEquals("[\n" +
        " null\n" +
        "]", prettify);
  }
}