package rocks.appconcept.javatools.parser.peg;

import junit.framework.TestCase;
import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstQuotedStringTest extends TestCase {

  public void testQuotedStrings() throws Exception {

    assertEquals(null, parse("'", '\''));
    assertEquals("", parse("''", '\''));
    assertEquals("apa", parse("'apa'", '\''));
    assertEquals("apa's", parse("'apa\\'s'", '\''));
    assertEquals("apa's", parse("\"apa\\'s\"", '"'));
    assertEquals("a\\b", parse("\"a\\\\b\"", '"'));
    assertEquals(null, parse("'a\\'", '\''));
    assertEquals(null, parse("", '\''));
    assertEquals(null, parse("a", '\''));
    assertEquals(null, parse("\\", '\''));
    assertEquals(null, parse("\\'", '\''));
    assertEquals(null, parse("\\'", '\\'));
    assertEquals("abc", parse("\\abc\\", '\\'));

  }

  public void testRepeatParses() throws Exception {

    PegParser parser = new AstQuotedString('"');
    assertTrue(PegParser.FAILED == parser.parse(new Input("abcde", 0)));
    assertTrue(PegParser.FAILED == parser.parse(new Input("abcde", 0)));
  }

  private String parse(String text, char quote) {
    Input i = new Input(text, 0);
    PegParser parser = PegParser.string(quote);
    Output parse = parser.parse(i);
    if (parse == PegParser.FAILED || i.position != text.length()) {
      System.out.println("Syntax error: " + text.substring(i.position));
      return null;
    }
    return (String) parse.object;
  }
}