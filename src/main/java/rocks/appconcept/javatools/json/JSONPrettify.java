package rocks.appconcept.javatools.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import rocks.appconcept.javatools.parser.Patterns;

/**
 * @author yanimetaxas
 */
public class JSONPrettify {

  private static Map<String, String> meta;
  private static Pattern escapable = Pattern.compile(
      "[\\\\\"\u0000-\u001f\u007f-\u009f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]");

  private JSONPrettify() {
  }

  static {
    meta = new HashMap<>();
    meta.put("\b", "\\b");
    meta.put("\t", "\\t");
    meta.put("\n", "\\n");
    meta.put("\f", "\\f");
    meta.put("\r", "\\r");
    meta.put("\"", "\\\"");
    meta.put("\\", "\\\\");
  }


  public static String prettify(String json) {
    return prettify(json, "  ");
  }

  public static String prettify(String json, String indentation) {
    try {
      return prettify(JSONObject.parse(json), indentation);
    } catch (IllegalArgumentException iae) {
      return json;
    }
  }

  public static String prettify(JSONObject parse, String indentation) {
    StringBuilder out = new StringBuilder();
    print(parse, out, "", indentation);
    return out.toString();
  }


  private static void print(JSONObject parse, StringBuilder out, String prefix,
      String indentation) {
    if (parse == JSONObject.TRUE) {
      out.append("true");
    } else if (parse == JSONObject.FALSE) {
      out.append("false");
    } else if (parse == JSONObject.NULL || parse == JSONObject.UNDEFINED) {
      out.append("null");
    } else if (parse.isString()) {
      out.append("\"");
      quote(parse.asString(), out);
      out.append("\"");
    } else if (parse.isNumber()) {
      double obj = parse.asDouble();
      out.append(Double.isFinite(obj) ? String.valueOf(obj) : null);
    } else if (parse.isList()) {
      if (parse.length() == 0) {
        out.append("[]");
      } else {
        out.append("[\n");
        for (Iterator<JSONObject> iterator = parse.values().iterator(); iterator.hasNext(); ) {
          JSONObject child = iterator.next();
          out.append(prefix).append(indentation);
          print(child, out, prefix + indentation, indentation);
          if (iterator.hasNext()) {
            out.append(",");
          }
          out.append("\n");
        }
        out.append(prefix).append("]");
      }
    } else {
      out.append("{\n");
      for (Iterator<Map.Entry<String, JSONObject>> iterator = parse.entrySet().iterator();
          iterator.hasNext(); ) {
        Map.Entry<String, JSONObject> entry = iterator.next();
        out.append(prefix).append(indentation).append("\"");
        quote(entry.getKey(), out);
        out.append("\": ");
        print(entry.getValue(), out, prefix + indentation, indentation);
        if (iterator.hasNext()) {
          out.append(",");
        }
        out.append("\n");
      }
      out.append(prefix).append("}");
    }
  }

  private static void quote(String s, StringBuilder out) {
    Patterns.replaceAll(escapable, s, out, r -> {
      if (meta.containsKey(r.group())) {
        return meta.get(r.group());
      }
      String x = "0000" + Integer.toHexString(r.group().charAt(0));
      return "\\u" + (x.substring(x.length() - 4));
    });
  }
}
