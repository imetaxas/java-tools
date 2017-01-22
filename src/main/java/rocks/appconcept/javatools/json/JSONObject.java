package rocks.appconcept.javatools.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON Parser Class. This object attempts to imitate the ways Javascript handles objects.
 *
 * @since 2011-09-09 14:26
 */
public abstract class JSONObject {

  public static final JSONObject TRUE = new JSONObjectBoolean(true);
  public static final JSONObject FALSE = new JSONObjectBoolean(false);
  public static final JSONObject NULL = new JSONObjectNull();
  public static final JSONObject UNDEFINED = new JSONObjectUndefined();

  /**
   * Parses a JSON string into a JSONObject.
   *
   * @param json The JSON string.
   * @return The new object.
   */
  public static JSONObject parse(String json) {
    return new JSONObjectParser().read(json);
  }

  public static JSONObject object() {
    return new JSONObjectObject(new HashMap<String, JSONObject>());
  }

  public static JSONObject list() {
    return new JSONObjectList(new ArrayList<JSONObject>());
  }

  public static JSONObject bool(boolean val) {
    return val ? TRUE : FALSE;
  }

  public static JSONObject number(Number val) {
    return new JSONObjectNumber(val);
  }

  public static JSONObject string(String val) {
    return new JSONObjectString(val);
  }

  /**
   * Returns the property with the specified index. Useful for lists.
   *
   * @param index The index to return.
   * @return The value of the property.
   */
  public JSONObject get(int index) {
    throw new IllegalStateException("Cannot get index " + index + " of " + asString() + ".");
  }

  public void set(int index, JSONObject value) {
    throw new IllegalStateException("Cannot set index " + index + " of " + asString() + ".");
  }

  public final void set(int index, boolean val) {
    set(index, bool(val));
  }

  public final void set(int index, Number val) {
    set(index, number(val));
  }

  public final void set(int index, String val) {
    set(index, string(val));
  }

  /**
   * Returns the property with the specified name. Useful for objects.
   *
   * @param property The name of the property.
   * @return The value of the property.
   */
  public JSONObject get(String property) {
    throw new IllegalStateException("Cannot get property " + property + " of " + asString() + ".");
  }

  public void set(String property, JSONObject value) {
    throw new IllegalStateException("Cannot set property " + property + " of " + asString() + ".");
  }

  public final void set(String property, boolean val) {
    set(property, bool(val));
  }

  public final void set(String property, Number val) {
    set(property, number(val));
  }

  public final void set(String property, String val) {
    set(property, string(val));
  }

  public abstract String toJSON();

  public final String toString() {
    return toJSON();
  }

  /**
   * Coerce this object into a boolean value.
   *
   * @return true or false.
   */
  public abstract boolean asBoolean();

  /**
   * Coerce this object into a Double.
   *
   * @return The value or null.
   */
  public abstract Double asDouble();

  /**
   * Coerce this object into a Float.
   *
   * @return The value or null.
   */
  public abstract Float asFloat();

  /**
   * Coerce this object into an Integer.
   *
   * @return The value or null.
   */
  public abstract Integer asInteger();

  /**
   * Coerce this object into a Long.
   *
   * @return The value or null.
   */
  public abstract Long asLong();

  /**
   * Coerce this object into a String.
   *
   * @return The value or null.
   */
  public abstract String asString();

  /**
   * Returns the length of the object.
   *
   * @return The length of the object, or 0 if the object isn't of list type.
   */
  public int length() {
    return 0;
  }

  /**
   * @return Is this an object
   */
  public boolean isObject() {
    return false;
  }

  /**
   * @return Is this a String
   */
  public boolean isString() {
    return false;
  }

  /**
   * @return Is this a List
   */
  public boolean isList() {
    return false;
  }

  /**
   * @return Is this a Number
   */
  public boolean isNumber() {
    return false;
  }


  public final JSONObject or(JSONObject that) {
    return this.asBoolean() ? this : that;
  }

  public final boolean or(boolean that) {
    return this.asBoolean() ? this.asBoolean() : that;
  }

  public final double or(double that) {
    return this.asBoolean() ? this.asDouble() : that;
  }

  public final float or(float that) {
    return this.asBoolean() ? this.asFloat() : that;
  }

  public final int or(int that) {
    return this.asBoolean() ? this.asInteger() : that;
  }

  public final long or(long that) {
    return this.asBoolean() ? this.asLong() : that;
  }

  public final String or(String that) {
    return this.asBoolean() ? this.asString() : that;
  }

  /**
   * If this is an object, returns a {@link Set} view of the mappings contained in this object.
   * The set is backed by the object, so changes to the object are
   * reflected in the set, and vice-versa.  If the object is modified
   * while an iteration over the set is in progress (except through
   * the iterator's own <tt>remove</tt> operation, or through the
   * <tt>setValue</tt> operation on a map entry returned by the
   * iterator) the results of the iteration are undefined.  The set
   * supports element removal, which removes the corresponding
   * value from the object, via the <tt>Iterator.remove</tt>,
   * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
   * <tt>clear</tt> operations.  It does not support the
   * <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a set view of the mappings contained in this object, or an empty set if this is not an
   * object.
   */
  public Set<Map.Entry<String, JSONObject>> entrySet() {
    return Collections.emptySet();
  }

  /**
   * If this is an object or a list, returns a {@link Collection} view of the values contained in
   * this object. The collection is backed by the object or list, so changes to that are reflected
   * in the collection, and vice-versa.  If the object or list is modified while an iteration over
   * the collection is in progress (except through the iterator's own <tt>remove</tt> operation),
   * the results of the iteration are undefined.  The collection supports element removal, which
   * removes the corresponding value from the object or list, shifting any subsequent elements to
   * the left in the case of a list, via the <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not support the
   * <tt>add</tt> or <tt>addAll</tt> operations for objects, but does for lists.
   *
   * @return a collection view of the values contained in this object or list, or an empty
   * collection if this is not an object or a list.
   */
  public Collection<JSONObject> values() {
    return Collections.emptySet();
  }

  private static class JSONObjectBoolean extends JSONObject {

    private boolean value;

    public JSONObjectBoolean(boolean value) {
      this.value = value;
    }

    public boolean asBoolean() {
      return value;
    }

    public Double asDouble() {
      return value ? 1.0 : 0.0;
    }

    public Float asFloat() {
      return value ? 1.0f : 0.0f;
    }

    public Integer asInteger() {
      return value ? 1 : 0;
    }

    public Long asLong() {
      return value ? 1L : 0L;
    }

    public String asString() {
      return value ? "true" : "false";
    }

    public String toJSON() {
      return asString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
      JSONObjectBoolean that = (JSONObjectBoolean) o;
      return value == that.value;
    }

    public int hashCode() {
      return (value ? 1 : 0);
    }
  }

  private static class JSONObjectNull extends JSONObject {

    public boolean asBoolean() {
      return false;
    }

    public Double asDouble() {
      return Double.NaN;
    }

    public Float asFloat() {
      return Float.NaN;
    }

    public Integer asInteger() {
      return null;
    }

    public Long asLong() {
      return null;
    }

    public String asString() {
      return "null";
    }

    public String toJSON() {
      return "null";
    }
  }

  private static class JSONObjectUndefined extends JSONObject {

    public boolean asBoolean() {
      return false;
    }

    public Double asDouble() {
      return Double.NaN;
    }

    public Float asFloat() {
      return Float.NaN;
    }

    public Integer asInteger() {
      return null; // Todo: There is no NaN for integers
    }

    public Long asLong() {
      return null;
    }

    public String asString() {
      return null;
    }

    @Override
    public String toJSON() {
      throw new IllegalStateException("Undefined has no representation in JSON");
    }
  }

  private static class JSONObjectString extends JSONObject {

    private String value;

    public JSONObjectString(String value) {
      this.value = value;
    }

    public boolean asBoolean() {
      return !value.isEmpty();
    }

    public Double asDouble() {
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException e) {
        return Double.NaN;
      }
    }

    public Float asFloat() {
      try {
        return Float.parseFloat(value);
      } catch (NumberFormatException e) {
        return Float.NaN;
      }
    }

    public Integer asInteger() {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        return null;
      }
    }

    public Long asLong() {
      try {
        return Long.valueOf(value);
      } catch (NumberFormatException e) {
        return null;
      }
    }

    public String asString() {
      return value;
    }

    public boolean isString() {
      return true;
    }

    public String toJSON() {
      return new JSONStream().value(value).toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
      JSONObjectString that = (JSONObjectString) o;
      return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    public int hashCode() {
      return value != null ? value.hashCode() : 0;
    }
  }

  private static class JSONObjectNumber extends JSONObject {

    private Number value;

    public JSONObjectNumber(Number number) {
      this.value = number;
    }

    public boolean asBoolean() {
      return value.intValue() != 0;
    }

    public Double asDouble() {
      return value.doubleValue();
    }

    public Float asFloat() {
      return value.floatValue();
    }

    public Integer asInteger() {
      return value.intValue();
    }

    public Long asLong() {
      return value.longValue();
    }

    public String asString() {
      return value.toString();
    }

    public boolean isNumber() {
      return true;
    }

    public String toJSON() {
      return asString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

      JSONObjectNumber that = (JSONObjectNumber) o;
      return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    public int hashCode() {
      return value != null ? value.hashCode() : 0;
    }
  }

  private static class JSONObjectObject extends JSONObject {

    private Map<String, JSONObject> value;

    public JSONObjectObject(Map<String, JSONObject> object) {
      this.value = object;
    }

    public JSONObject get(String property) {
      return value.containsKey(property) ? value.get(property) : JSONObject.UNDEFINED;
    }

    public void set(String property, JSONObject value) {
      this.value.put(property, value);
    }

    public boolean asBoolean() {
      return true;
    }

    public Double asDouble() {
      return Double.NaN;
    }

    public Float asFloat() {
      return Float.NaN;
    }

    public Integer asInteger() {
      return null; // Todo: There is no NaN for integers
    }

    public Long asLong() {
      return null;
    }

    public String asString() {
      return "[object Object]";
    }

    public boolean isObject() {
      return true;
    }

    public String toJSON() {
      JSONStream out = new JSONStream();
      out.object();
      for (Map.Entry<String, JSONObject> entry : value.entrySet()) {
        out.verbatim(entry.getKey(), entry.getValue().toJSON());
      }
      out.endobject();
      return out.toString();
    }

    public Set<Map.Entry<String, JSONObject>> entrySet() {
      return value.entrySet();
    }

    public Collection<JSONObject> values() {
      return value.values();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

      JSONObjectObject that = (JSONObjectObject) o;
      return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    public int hashCode() {
      return value != null ? value.hashCode() : 0;
    }
  }

  private static class JSONObjectList extends JSONObject {

    private List<JSONObject> value;

    public JSONObjectList(List<JSONObject> array) {
      this.value = array;
    }

    public JSONObject get(int index) {
      return index >= 0 && index < value.size() ? value.get(index) : JSONObject.UNDEFINED;
    }

    public void set(int index, JSONObject value) {
      this.value.set(index, value);
    }

    public boolean asBoolean() {
      return true;
    }

    public Double asDouble() {
      return Double.NaN;
    }

    public Float asFloat() {
      return Float.NaN;
    }

    public Integer asInteger() {
      return null;
    }

    public Long asLong() {
      return null;
    }

    public String asString() {
      return value.toString();
    }

    public int length() {
      return value.size();
    }

    public boolean isList() {
      return true;
    }

    public String toJSON() {
      JSONStream out = new JSONStream();
      out.list();
      for (JSONObject jsonObject : value) {
        out.verbatim(jsonObject.toJSON());
      }
      out.endlist();
      return out.toString();
    }

    public Collection<JSONObject> values() {
      return value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

      JSONObjectList that = (JSONObjectList) o;
      return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    public int hashCode() {
      return value != null ? value.hashCode() : 0;
    }
  }

  private static class JSONObjectParser {

    public JSONObject read(String json) {
        if (json == null) {
            return JSONObject.NULL;
        }
      return read(new StringCharacterIterator(json));
    }

    public JSONObject read(CharacterIterator json) {
      skipWhitespace(json);
      JSONObject value = value(json);
      skipWhitespace(json);
        if (json.current() != CharacterIterator.DONE) {
            exception(json);
        }
      return value;
    }

    private JSONObject value(CharacterIterator json) {
      char c = c(json);
      if (c == '"') {
        return new JSONObjectString(string(json));
      } else if (Character.isDigit(c) || c == '-') {
        json.previous();
        return new JSONObjectNumber(number(json));
      } else if (c == '{') {
        return new JSONObjectObject(object(json));
      } else if (c == '[') {
        return new JSONObjectList(array(json));
      } else if (c == 't') {
          if (c(json) == 'r' && c(json) == 'u' && c(json) == 'e') {
              return JSONObject.TRUE;
          }
      } else if (c == 'f') {
          if (c(json) == 'a' && c(json) == 'l' && c(json) == 's' && c(json) == 'e') {
              return JSONObject.FALSE;
          }
      } else if (c == 'n') {
          if (c(json) == 'u' && c(json) == 'l' && c(json) == 'l') {
              return JSONObject.NULL;
          }
      }
      exception(json);
      return null;
    }

    private String string(CharacterIterator json) {
      StringBuilder s = new StringBuilder();
      char c;
      while ((c = c(json)) != '"') {
        if (c == '\\') {
          c = c(json);
            if (c == '"') {
                s.append('"');
            } else if (c == '\\') {
                s.append('\\');
            } else if (c == '/') {
                s.append('/');
            } else if (c == 'b') {
                s.append('\b');
            } else if (c == 'f') {
                s.append('\f');
            } else if (c == 'n') {
                s.append('\n');
            } else if (c == 'r') {
                s.append('\r');
            } else if (c == 't') {
                s.append('\t');
            } else if (c == 'u') {
                s.append(unicode(json));
            } else {
                exception(json);
            }
        } else if (Character.isValidCodePoint(c) && !Character.isISOControl(c)) {
          s.append(c);
        } else {
          exception(json);
        }
      }
      return s.toString();
    }

    private Number number(CharacterIterator json) {
      int length = 0;
      boolean isFloatingPoint = false;

      StringBuilder s = new StringBuilder();
        if (c(json) == '-') {
            s.append('-');
        } else {
            json.previous();
        }

      length += addDigits(json, s);

      if (s.length() >= 3 && s.charAt(0) == '-' && s.charAt(1) == '0' && Character
          .isDigit(s.charAt(2)) || s.length() >= 2 && s.charAt(0) == '0' && Character
          .isDigit(s.charAt(1))) {
        exception(json);
      }

      char c = cOrDone(json);
      if (c == '.') {
        s.append(c);
        length += addDigits(json, s);
        isFloatingPoint = true;
      } else if (c != CharacterIterator.DONE) {
        json.previous();
      }
      c = cOrDone(json);
      if (c == 'e' || c == 'E') {
        s.append(c);
        c = c(json);
          if (c == '+' || c == '-') {
              s.append(c);
          } else {
              json.previous();
          }
        addDigits(json, s);
        isFloatingPoint = true;
      } else if (c != CharacterIterator.DONE) {
        json.previous();
      }
      return isFloatingPoint
          ? (length < 17) ? Double.valueOf(s.toString()) : new BigDecimal(s.toString())
          : (length < 19) ? Long.valueOf(s.toString()) : new BigInteger(s.toString());
    }

    private Map<String, JSONObject> object(CharacterIterator json) {
      Map<String, JSONObject> map = new HashMap<String, JSONObject>();
      while (true) {
        skipWhitespace(json);
        char c = c(json);
          if (c == '}') {
              break;
          }
          if (c != '"') {
              exception(json);
          }
        String key = string(json);
        skipWhitespace(json);
          if (c(json) != ':') {
              exception(json);
          }
        skipWhitespace(json);
        JSONObject value = value(json);
        skipWhitespace(json);
        map.put(key, value);
        c = c(json);
        if (c != ',') {
          if (c == '}') {
            break;
          } else {
            exception(json);
          }
        }
      }

      return map;
    }

    private List<JSONObject> array(CharacterIterator json) {
      List<JSONObject> array = new ArrayList<JSONObject>();

      while (true) {
        skipWhitespace(json);
        char c = c(json);
          if (c == ']') {
              break;
          }
        json.previous();
        array.add(value(json));
        skipWhitespace(json);
        c = c(json);
        if (c != ',') {
          if (c == ']') {
            break;
          } else {
            exception(json);
          }
        }
      }

      return array;
    }

    private int addDigits(CharacterIterator json, StringBuilder s) {
      int ret;
      char c = c(json);
      for (ret = 0; Character.isDigit(c); ++ret, c = cOrDone(json)) {
        s.append(c);
      }
        if (c != CharacterIterator.DONE) {
            json.previous();
        }
      return ret;
    }

    private char unicode(CharacterIterator json) {
      return (char) ((hex(json) << 12) | (hex(json) << 8) | (hex(json) << 4) | hex(json));
    }

    private int hex(CharacterIterator json) {
      char c = c(json);
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
      exception(json);
      return 0;
    }

    private Object exception(CharacterIterator json) {
      json.previous();
      StringBuilder s = new StringBuilder(21);
      int index = json.getIndex();
      json.setIndex(Math.max(0, json.getIndex() - 20));
      for (int i = 0; i < 20; ++i) {
          if (json.getIndex() == index) {
              s.append("!");
          }
        s.append(json.current());
          if (json.next() == CharacterIterator.DONE) {
              break;
          }
      }
      throw new IllegalArgumentException(
          "Malformed JSON near index " + index + ". (" + s.toString() + ")");
    }

    private void skipWhitespace(CharacterIterator json) {
      char c;
      while (Character.isWhitespace(json.current())) {
        json.next();
      }
    }

    private char c(CharacterIterator json) {
      char c = json.current();
        if (c == CharacterIterator.DONE) {
            exception(json);
        }
      json.next();
      return c;
    }

    private char cOrDone(CharacterIterator json) {
      char c = json.current();
      if (c != CharacterIterator.DONE)
        json.next();
      return c;
    }
  }

}
