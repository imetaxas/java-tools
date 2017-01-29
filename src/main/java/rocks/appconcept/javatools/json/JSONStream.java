package rocks.appconcept.javatools.json;

/**
 * Utility class for writing JSON documents. Example:
 *
 * JSONStream json = new JSONStream();   // CURRENT STREAM: json.object();                        //
 * { json.value("prop1", "a string");      // {"prop":"a string" json.value("prop2", 12.4); //
 * {"prop":"a string","prop2":12.4 json.value("prop3", false);           // {"prop":"a
 * string","prop2":12.4,"prop3":false json.list("listprop");                // {"prop":"a
 * string","prop2":12.4,"prop3":false,"listprop":[ json.value("another string");         //
 * {"prop":"a string","prop2":12.4,"prop3":false,"listprop":["another string" json.value(12.5); //
 * {"prop":"a string","prop2":12.4,"prop3":false,"listprop":["another string",12.5 json.value(true);
 *                     // {"prop":"a string","prop2":12.4,"prop3":false,"listprop":["another
 * string",12.5,true json.endlist();                       // {"prop":"a
 * string","prop2":12.4,"prop3":false,"listprop":["another string",12.5,true] json.endobject(); //
 * {"prop":"a string","prop2":12.4,"prop3":false,"listprop":["another string",12.5,true]} String x =
 * json.toString();
 *
 * @author yanimetaxas
 */
@SuppressWarnings({"NullableProblems"})
public final class JSONStream {

  private static final int LIST = 0;
  private static final int OBJECT = 1;
  private static final int NOTFIRST = 2;
  private static final int SINGLE = 4;
  private static final char[] NULL_CHARS = "null".toCharArray();
  private static final char[] TRUE_CHARS = "true".toCharArray();
  private static final char[] FALSE_CHARS = "false".toCharArray();
  private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
  private static final int[] sOutputEscapes;

  static {
    int[] table = new int[256];
    for (int i = 0; i < 32; ++i) {
      table[i] = -(i + 1);
    }
    table['"'] = '"';
    table['/'] = '/';
    table['\\'] = '\\';
    table[0x08] = 'b';
    table[0x09] = 't';
    table[0x0C] = 'f';
    table[0x0A] = 'n';
    table[0x0D] = 'r';
    sOutputEscapes = table;
  }

  private final StringBuilder out = new StringBuilder();
  private final int[] stack = new int[100];
  private int stackptr = 0;

  public JSONStream() {
    stack[0] = SINGLE;
  }

  public static class JSONException extends RuntimeException {

    public JSONException(String message) {
      super(message);
    }
  }

  /**
   * Write the start of an object: **{**
   *
   * - This method can only be called if the current context is EMPTY or a LIST.
   * - After this method returns the current context becomes OBJECT.
   *
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is OBJECT.
   */
  public JSONStream object() {
    if (isObject()) {
      throw new JSONException("Properties in objects must have a name");
    }
    return object(null);
  }

  /**
   * Write the start of an object in an OBJECT context: **"prop":{**
   *
   * - This method can only be called if the current context is OBJECT.
   * - After this method returns the current context becomes OBJECT.
   *
   * @param prop The name of the property (must not be ##null##).
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is not OBJECT.
   */
  public JSONStream object(String prop) {
    if (isComplete()) {
      throw new JSONException("No more objects can be created at this point");
    }
    if (prop != null && !isObject()) {
      throw new JSONException("Property without object");
    }
    if (isNotFirst()) {
      out.append(",");
    }
    if (prop != null) {
      string(prop);
      out.append(":");
    }
    out.append("{");
    if (stackptr >= 0) {
      stack[stackptr] |= NOTFIRST;
    }
    stack[++stackptr] = OBJECT;
    return this;
  }

  /**
   * Write verbatim JSON.
   *
   * - This method can only be called if the current context is EMPTY or LIST.
   *
   * @param json The JSON to write
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is OBJECT.
   */
  public JSONStream verbatim(String json) {
    if (isObject()) {
      throw new JSONException("Properties in objects must have a name");
    }
    return verbatim(null, json);
  }

  /**
   * Write verbatim JSON in an OBJECT context: **"prop":JSON**
   *
   * - This method can only be called if the current context is OBJECT.
   *
   * @param prop The name of the property (must not be ##null##).
   * @param json The JSON to write.
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is not OBJECT.
   */
  public JSONStream verbatim(String prop, String json) {
    if (isComplete()) {
      throw new JSONException("No more verbatim data can be output at this point");
    }
    if (prop != null && !isObject()) {
      throw new JSONException("Property without object");
    }
    if (isNotFirst()) {
      out.append(",");
    }
    if (prop != null) {
      string(prop);
      out.append(":");
    }
    out.append(json);
    if (stackptr >= 0) {
      stack[stackptr] |= NOTFIRST;
    }
    return this;
  }

  /**
   * Write the start of a list: **[**
   *
   * - This method can only be called if the current context is EMPTY or a LIST.
   * - After this method returns the current context becomes LIST.
   *
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is OBJECT.
   */
  public JSONStream list() {
    if (isObject()) {
      throw new JSONException("Properties in objects must have a name");
    }
    return list(null);
  }

  /**
   * Write the start of a list in an OBJECT context: **"prop":[**
   *
   * - This method can only be called if the current context is OBJECT.
   * - After this method returns the current context becomes LIST.
   *
   * @param prop The name of the property (must not be ##null##).
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is not OBJECT.
   */
  public JSONStream list(String prop) {
    if (isComplete()) {
      throw new JSONException("No more list can be created at this point");
    }
    if (prop != null && !isObject()) {
      throw new JSONException("Property without object");
    }
    if (isNotFirst()) {
      out.append(",");
    }
    if (prop != null) {
      string(prop);
      out.append(":");
    }
    out.append("[");
    if (stackptr >= 0) {
      stack[stackptr] |= NOTFIRST;
    }
    stack[++stackptr] = LIST;
    return this;
  }

  /**
   * Write the end of a list or object depending on the current context.
   *
   * - This method can only be called if the current context is OBJECT or LIST. - After this method
   * returns the current context is restored to what it was before the object or list was opened.
   *
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is EMPTY.
   */
  public JSONStream end() {
    if (stackptr <= 0) {
      throw new JSONException("Close without open");
    }
    out.append(isObject() ? "}" : "]");
    stackptr--;
    return this;
  }

  /**
   * Repeatedly write the end of a list or object depending on the current context until the
   * document is valid.
   *
   * - This method can only be called if the current context is OBJECT or LIST. - After this method
   * returns the current context is EMPTY and no more writes should be made.
   *
   * @return The "this" JSONStream object, for easy chaining of calls.
   */
  public JSONStream endall() {
    while (stackptr > 0) {
      end();
    }
    return this;
  }

  /**
   * Write the end of a list: **]**
   *
   * - This method can only be called if the current context is LIST. - After this method returns
   * the current context is restored to what it was before the list was opened.
   *
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is not LIST.
   */
  public JSONStream endlist() {
    if (stackptr <= 0) {
      throw new JSONException("Close list without open");
    }
    out.append("]");
    stackptr--;
    return this;
  }

  /**
   * Write the end of an object: **}**
   *
   * - This method can only be called if the current context is OBJECT. - After this method returns
   * the current context is restored to what it was before the object was opened.
   *
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is not OBJECT.
   */
  public JSONStream endobject() {
    if (stackptr <= 0) {
      throw new JSONException("Close object without open");
    }
    out.append("}");
    stackptr--;
    return this;
  }

  /**
   * Write a string value (also properly escapes the string as JSON): **STRING**
   *
   * - This method can only be called if the current context is LIST.
   *
   * @param str The string to write
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is OBJECT.
   */
  public JSONStream value(String str) {
    if (isObject()) {
      throw new JSONException("Properties in objects must have a name");
    }
    return value(null, str);
  }

  /**
   * Write a string value in an object context (also properly escapes the string as JSON):
   * **"prop":STRING**
   *
   * - This method can only be called if the current context is OBJECT.
   *
   * @param prop The name of the property (must not be ##null##).
   * @param str The string to write
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is not OBJECT.
   */
  public JSONStream value(String prop, String str) {
    if (isComplete()) {
      throw new JSONException("No more values can be created at this point");
    }
    if (prop != null && !isObject()) {
      throw new JSONException("Property without object");
    }
    if (isNotFirst()) {
      out.append(",");
    }
    if (prop != null) {
      string(prop);
      out.append(":");
    }
    string(str);
    stack[stackptr] |= NOTFIRST;
    return this;
  }

  /**
   * Write a boolean value: **true** or **false**
   *
   * - This method can only be called if the current context is LIST.
   *
   * @param obj The boolean to write
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is OBJECT.
   */
  public JSONStream value(boolean obj) {
    if (isObject()) {
      throw new JSONException("Properties in objects must have a name");
    }
    return value(null, obj);
  }

  /**
   * Write a boolean value in an object context: **"prop":true** or **"prop":false**
   *
   * - This method can only be called if the current context is OBJECT.
   *
   * @param prop The name of the property (must not be ##null##).
   * @param obj The boolean to write
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is not OBJECT.
   */
  public JSONStream value(String prop, boolean obj) {
    if (prop != null && !isObject()) {
      throw new JSONException("Property without object");
    }
    if (isNotFirst()) {
      out.append(",");
    }
    if (prop != null) {
      string(prop);
      out.append(":");
    }
    out.append(obj ? TRUE_CHARS : FALSE_CHARS);
    stack[stackptr] |= NOTFIRST;
    return this;
  }

  /**
   * Write a number value: **NUMBER**
   *
   * - This method can only be called if the current context is LIST.
   *
   * @param obj The number to write
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is OBJECT.
   */
  public JSONStream value(double obj) {
    if (isObject()) {
      throw new JSONException("Properties in objects must have a name");
    }
    if (Double.isInfinite(obj) || Double.isNaN(obj)) {
      return value(null, Double.toString(obj));
    }
    return value(null, obj);
  }

  /**
   * Write a number value in an object context: **"prop":NUMBER**
   *
   * - This method can only be called if the current context is OBJECT.
   *
   * @param prop The name of the property (must not be ##null##).
   * @param obj The number to write
   * @return The "this" JSONStream object, for easy chaining of calls.
   * @throws JSONException if the current context is not OBJECT.
   */
  public JSONStream value(String prop, double obj) {
    if (prop != null && !isObject()) {
      throw new JSONException("Property without object");
    }
    if (Double.isInfinite(obj) || Double.isNaN(obj)) {
      return value(prop, Double.toString(obj));
    }
    if (isNotFirst()) {
      out.append(",");
    }
    if (prop != null) {
      string(prop);
      out.append(":");
    }
    out.append(String.valueOf(obj));
    int length = out.length();
    if (length > 2 && out.charAt(length - 1) == '0' && out.charAt(length - 2) == '.') {
      out.setLength(length - 2);
    }
    stack[stackptr] |= NOTFIRST;
    return this;
  }

  private void string(String content) {
    if (content == null) {
      out.append(NULL_CHARS);
    } else {
      final int[] escCodes = sOutputEscapes;
      int escLen = escCodes.length;

      out.append('"');
      for (int i = 0, len = content.length(); i < len; ++i) {
        char c = content.charAt(i);
        if (c == 0x2028) {
          out.append("\\u2028");
          continue;
        } else if (c == 0x2029) {
          out.append("\\u2029");
          continue;
        } else if (c >= escLen || escCodes[c] == 0) {
          out.append(c);
          continue;
        }
        out.append('\\');
        int escCode = escCodes[c];
        if (escCode < 0) { // generic quoting (hex value)
          // We know that it has to fit in just 2 hex chars
          out.append('u');
          out.append('0');
          out.append('0');
          int value = -(escCode + 1);
          out.append(HEX_CHARS[value >> 4]);
          out.append(HEX_CHARS[value & 0xF]);
        } else { // "named", i.e. prepend with slash
          out.append((char) escCode);
        }
      }
      out.append('"');
    }
  }

  private boolean isObject() {
    return stackptr > 0 && (stack[stackptr] & OBJECT) != 0;
  }

  private boolean isNotFirst() {
    return stackptr >= 0 && (stack[stackptr] & NOTFIRST) != 0;
  }

  private boolean isComplete() {
    return (stack[stackptr] & (NOTFIRST | SINGLE)) == (NOTFIRST | SINGLE);
  }

  /**
   * Returns the JSON object as a String
   *
   * @return The JSON object
   * @throws JSONException if the current state is not EMPTY (unclosed objects or lists).
   */
  public String toString() {
    if (stackptr > 0) {
      throw new JSONException("Unclosed json");
    }
    return out.toString();
  }

  /**
   * Returns the JSON object as a String
   *
   * @return The JSON object
   * @throws JSONException if the current state is not EMPTY (unclosed objects or lists).
   */
  public String toJSON() {
    return toString();
  }
}
