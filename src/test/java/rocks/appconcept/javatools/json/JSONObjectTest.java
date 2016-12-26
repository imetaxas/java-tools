package rocks.appconcept.javatools.json;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * @author yanimetaxas
 */
public class JSONObjectTest extends TestCase {
    public void testSimpleString() throws Exception {
        JSONObject parsed = JSONObject.parse("\"Value\"");
        assertTrue(parsed.isString());
        assertFalse(parsed.isNumber());
        assertFalse(parsed.isObject());
        assertFalse(parsed.isList());

        assertEquals("Value", parsed.asString());
        assertTrue(parsed.asDouble().isNaN());
        assertTrue(parsed.asFloat().isNaN());
        assertNull(parsed.asInteger());
        assertTrue(parsed.asBoolean());

        assertEquals("\"Value\"", parsed.toJSON());

        assertEquals(0, parsed.length());
        assertEquals(0, parsed.values().size());
        assertEquals(0, parsed.entrySet().size());
        try {
            parsed.get(0);
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("d");
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("0");
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testNumberString() throws Exception {
        JSONObject parsed = JSONObject.parse("\"17.53\"");
        assertTrue(parsed.isString());
        assertFalse(parsed.isNumber());
        assertFalse(parsed.isObject());
        assertFalse(parsed.isList());

        assertEquals("17.53", parsed.asString());
        assertEquals(17.53, parsed.asDouble());
        assertEquals(17.53f, parsed.asFloat());
        assertTrue(parsed.asInteger() == null); // TODO: ???
        assertEquals(17, (int) JSONObject.parse("\"17\"").asInteger());
        assertTrue(parsed.asBoolean());
        assertTrue(JSONObject.parse("\"0\"").asBoolean());

        assertEquals("\"17.53\"", parsed.toJSON());

        assertEquals(0, parsed.length());
        assertEquals(0, parsed.values().size());
        assertEquals(0, parsed.entrySet().size());
        try {
            parsed.get(0);
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("d");
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("0");
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testInt() throws Exception {
        JSONObject parsed = JSONObject.parse("17");
        assertFalse(parsed.isString());
        assertTrue(parsed.isNumber());
        assertFalse(parsed.isObject());
        assertFalse(parsed.isList());

        assertEquals("17", parsed.asString());
        assertEquals(17.0, parsed.asDouble());
        assertEquals(17.0f, parsed.asFloat());
        assertEquals(17, (int) parsed.asInteger());
        assertTrue(parsed.asBoolean());
        assertFalse(JSONObject.parse("0").asBoolean());

        assertEquals("17", parsed.toJSON());

        assertEquals(0, parsed.length());
        assertEquals(0, parsed.values().size());
        assertEquals(0, parsed.entrySet().size());
        try {
            parsed.get(0);
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("d");
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("0");
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testFloat() throws Exception {
        JSONObject parsed = JSONObject.parse("17.53");
        assertFalse(parsed.isString());
        assertTrue(parsed.isNumber());
        assertFalse(parsed.isObject());
        assertFalse(parsed.isList());

        assertEquals("17.53", parsed.asString());
        assertEquals(17.53, parsed.asDouble());
        assertEquals(17.53f, parsed.asFloat());
        assertEquals(17, (int) parsed.asInteger());
        assertTrue(parsed.asBoolean());
        assertFalse(JSONObject.parse("0.0").asBoolean());

        assertEquals("17.53", parsed.toJSON());

        assertEquals(0, parsed.length());
        assertEquals(0, parsed.values().size());
        assertEquals(0, parsed.entrySet().size());
        try {
            parsed.get(0);
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("d");
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("0");
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testSimpleObject() throws Exception {
        JSONObject parsed = JSONObject.parse("{\"a\" : 17.53, \"b\" : \"c\" }");
        assertFalse(parsed.isString());
        assertFalse(parsed.isNumber());
        assertTrue(parsed.isObject());
        assertFalse(parsed.isList());

        assertEquals("[object Object]", parsed.asString());
        assertTrue(parsed.asDouble().isNaN());
        assertTrue(parsed.asFloat().isNaN());
        assertNull(parsed.asInteger());
        assertTrue(parsed.asBoolean());

        String actual = parsed.toJSON();
        assertTrue(actual.contains("\"a\":17.53"));
        assertTrue(actual.contains("\"b\":\"c\""));

        assertEquals(0, parsed.length());
        assertEquals(2, parsed.values().size());
        assertEquals(new HashSet<>(Arrays.asList(JSONObject.string("c"), JSONObject.number(17.53))), new HashSet<>(parsed.values()));
        assertEquals(2, parsed.entrySet().size());
        /*//noinspection unchecked
        assertEquals(new HashSet<Map.Entry<String, JSONObject>>(Arrays.asList(
                new AbstractMap.SimpleEntry<String, JSONObject>("b", JSONObject.string("c")),
                new AbstractMap.SimpleEntry<String, JSONObject>("a", JSONObject.number(17.53))
        )), parsed.entrySet());*/
        try {
            parsed.get(0);
            fail();
        } catch (Exception ignored) {
        }
        assertSame(JSONObject.UNDEFINED, parsed.get("d"));
        assertSame(JSONObject.UNDEFINED, parsed.get("0"));
        assertEquals(JSONObject.number(17.53), parsed.get("a"));
        assertEquals(JSONObject.string("c"), parsed.get("b"));
    }

    public void testSimpleList() throws Exception {
        JSONObject parsed = JSONObject.parse("[\"a\", 17.53, \"b\"]");
        assertFalse(parsed.isString());
        assertFalse(parsed.isNumber());
        assertFalse(parsed.isObject());
        assertTrue(parsed.isList());

        assertEquals("[\"a\", 17.53, \"b\"]", parsed.asString());
        assertTrue(parsed.asDouble().isNaN());
        assertTrue(parsed.asFloat().isNaN());
        assertNull(parsed.asInteger());
        assertTrue(parsed.asBoolean());

        assertEquals("[\"a\",17.53,\"b\"]", parsed.toJSON());

        assertEquals(3, parsed.length());
        assertEquals(3, parsed.values().size());
        assertEquals(Arrays.asList(JSONObject.string("a"), JSONObject.number(17.53), JSONObject.string("b")), parsed.values());
        assertEquals(0, parsed.entrySet().size());
        assertEquals(JSONObject.string("a"), parsed.get(0));
        assertEquals(JSONObject.number(17.53), parsed.get(1));
        assertEquals(JSONObject.string("b"), parsed.get(2));
        try {
            parsed.get("d");
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("0");
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testNull() throws Exception {
        JSONObject parsed = JSONObject.parse("null");
        assertFalse(parsed.isString());
        assertFalse(parsed.isNumber());
        assertFalse(parsed.isObject());
        assertFalse(parsed.isList());
        assertSame(JSONObject.NULL, parsed);

        assertEquals("null", parsed.asString());
        assertEquals(Double.NaN, parsed.asDouble());
        assertEquals(Float.NaN, parsed.asFloat());
        assertNull(parsed.asInteger());
        assertFalse(parsed.asBoolean());

        assertEquals("null", parsed.toJSON());

        assertEquals(0, parsed.length());
        assertEquals(0, parsed.values().size());
        assertEquals(0, parsed.entrySet().size());
        try {
            parsed.get(0);
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("d");
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("0");
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testUndefined() throws Exception {
        JSONObject parsed = JSONObject.UNDEFINED;
        assertFalse(parsed.isString());
        assertFalse(parsed.isNumber());
        assertFalse(parsed.isObject());
        assertFalse(parsed.isList());

        assertEquals(null, parsed.asString());
        assertEquals(Double.NaN, parsed.asDouble());
        assertEquals(Float.NaN, parsed.asFloat());
        assertNull(parsed.asInteger());
        assertFalse(parsed.asBoolean());

        try {
            parsed.toJSON();
            fail();
        } catch (Exception ignored) {
        }

        assertEquals(0, parsed.length());
        assertEquals(0, parsed.values().size());
        assertEquals(0, parsed.entrySet().size());
        try {
            parsed.get(0);
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("d");
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("0");
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testTrue() throws Exception {
        JSONObject parsed = JSONObject.parse("true");
        assertFalse(parsed.isString());
        assertFalse(parsed.isNumber());
        assertFalse(parsed.isObject());
        assertFalse(parsed.isList());
        assertSame(JSONObject.TRUE, parsed);

        assertEquals("true", parsed.asString());
        assertEquals(1.0, parsed.asDouble());
        assertEquals(1.0f, parsed.asFloat());
        assertEquals(1, (int) parsed.asInteger());
        assertTrue(parsed.asBoolean());

        assertEquals("true", parsed.toJSON());

        assertEquals(0, parsed.length());
        assertEquals(0, parsed.values().size());
        assertEquals(0, parsed.entrySet().size());
        try {
            parsed.get(0);
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("d");
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("0");
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testFalse() throws Exception {
        JSONObject parsed = JSONObject.parse("false");
        assertFalse(parsed.isString());
        assertFalse(parsed.isNumber());
        assertFalse(parsed.isObject());
        assertFalse(parsed.isList());
        assertSame(JSONObject.FALSE, parsed);

        assertEquals("false", parsed.asString());
        assertEquals(0.0, parsed.asDouble());
        assertEquals(0.0f, parsed.asFloat());
        assertEquals(0, (int) parsed.asInteger());
        assertFalse(parsed.asBoolean());

        assertEquals("false", parsed.toJSON());

        assertEquals(0, parsed.length());
        assertEquals(0, parsed.values().size());
        assertEquals(0, parsed.entrySet().size());
        try {
            parsed.get(0);
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("d");
            fail();
        } catch (Exception ignored) {
        }
        try {
            parsed.get("0");
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testObjectManipulation() throws Exception {
        JSONObject parsed = JSONObject.parse("{\"a\" : 17.53, \"b\" : \"c\" }");
        parsed.set("a", 12);
        assertEquals(2, parsed.entrySet().size());
        assertEquals(12, (int) parsed.get("a").asInteger());
        parsed.set("c", JSONObject.object());
        assertEquals(3, parsed.entrySet().size());
        parsed.get("c").set("qw", "er");
        assertEquals("er", parsed.get("c").get("qw").asString());
        for (Iterator<Map.Entry<String, JSONObject>> iterator = parsed.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, JSONObject> entry = iterator.next();
            if (entry.getKey().equals("b")) iterator.remove();
        }
        assertEquals(2, parsed.entrySet().size());
        parsed.entrySet().clear();
        assertEquals(0, parsed.entrySet().size());
        assertEquals("{}", parsed.toJSON());
    }

    public void testOr() throws Exception {
        assertEquals("true", JSONObject.TRUE.or("false"));
        assertEquals(true, JSONObject.TRUE.or(false));
        assertEquals(1.0, JSONObject.TRUE.or(17.23));
        assertEquals(1.0f, JSONObject.TRUE.or(17.23f));
        assertEquals(1, JSONObject.TRUE.or(12));
        assertEquals(JSONObject.TRUE, JSONObject.TRUE.or(JSONObject.FALSE));

        assertEquals("true", JSONObject.FALSE.or("true"));
        assertEquals(true, JSONObject.FALSE.or(true));
        assertEquals(17.23, JSONObject.FALSE.or(17.23));
        assertEquals(17.23f, JSONObject.FALSE.or(17.23f));
        assertEquals(12, JSONObject.FALSE.or(12));
        assertEquals(JSONObject.TRUE, JSONObject.FALSE.or(JSONObject.TRUE));
    }

    public void testExpectedParseErrors() throws Exception {
        assertParseError("0123");
        assertParseError("-0123");
        assertParseError("abc");
        assertParseError("undefined");
        assertParseError("{\"a\": 17");
        assertParseError("{\"a\": \"b\"b}");
        assertParseError("{}\"a\": 17");
        assertParseError("{}17");
        assertParseError("\"ap\\ka\"");
        assertParseError("\"ap\\u1aAXa\"");
        assertParseError("[null null");
        assertParseError("12.");
    }

    private void assertParseError(String brokenJSON) {
        try {
            JSONObject.parse(brokenJSON);
            fail("Parsing this json should fail: " + brokenJSON);
        } catch (Exception ignored) {
        }
    }

    public void testUnicode() throws Exception {
        assertEquals('\u1234', JSONObject.parse("[\"\\u1234\"]").get(0).asString().charAt(0));
    }

    public void testNumbers() throws Exception {
        assertEquals("3.0E8", JSONObject.parse("3.e8").asFloat().toString());
        assertEquals("3.0E8", JSONObject.parse("3.E8").asFloat().toString());
    }
}
