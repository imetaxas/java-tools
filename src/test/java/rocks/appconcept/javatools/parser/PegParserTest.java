package rocks.appconcept.javatools.parser;

import java.util.List;
import junit.framework.TestCase;
import rocks.appconcept.javatools.parser.peg.Input;
import rocks.appconcept.javatools.parser.peg.Output;

/**
 * @author yanimetaxas
 */
public class PegParserTest extends TestCase {

  public void testVariousOperators() throws Exception {

    PegParser parser;
    Bean bean;

    parser = PegParser.character("abc");
    assertParse(parser, "a");
    assertParse(parser, "b");
    assertParse(parser, "c");
    assertWontParse(parser, " ");
    assertWontParse(parser, "aa");

    parser = PegParser.character("A");
    assertParse(parser, "A");
    assertWontParse(parser, "B");
    assertWontParse(parser, "a");

    parser = PegParser.whitespace(false);
    assertParse(parser, " ");
    assertParse(parser, "\t");
    assertParse(parser, "\n");
    assertParse(parser, "                            ");
    assertWontParse(parser, "");

    parser = PegParser.whitespace(true);
    assertParse(parser, " ");
    assertParse(parser, "\t");
    assertParse(parser, "\n");
    assertParse(parser, "                            ");
    assertParse(parser, "");
    assertWontParse(parser, "abc");

    parser = PegParser.optional(PegParser.literal("abc"));
    assertParse(parser, "");
    assertParse(parser, "abc");
    assertWontParse(parser, "cde");

    parser = PegParser.not(PegParser.literal("abc"));
    assertParse(parser, "123");
    assertParse(parser, "432");
    assertParse(parser, "");
    assertWontParse(parser, "abc");

    parser = PegParser
        .options(PegParser.literal("abacus"), PegParser.literal("aba"), PegParser.literal("cus"));
    assertParse(parser, "abacus");
    assertParse(parser, "aba");
    assertParse(parser, "cus");
    assertWontParse(parser, "abacusaba");

    parser = PegParser.dot();
    assertParse(parser, "1");
    assertParse(parser, "a");
    assertParse(parser, "b");
    assertWontParse(parser, "");

    parser = PegParser.digits();
    assertParse(parser, "0");
    assertParse(parser, "1");
    assertParse(parser, "2");
    assertParse(parser, "3");
    assertParse(parser, "4");
    assertParse(parser, "5");
    assertParse(parser, "6");
    assertParse(parser, "7");
    assertParse(parser, "8");
    assertParse(parser, "9");
    assertParse(parser, "111111119");
    assertWontParse(parser, "");
    assertWontParse(parser, "a");
    assertWontParse(parser, "b");
    assertWontParse(parser, "c");
    assertWontParse(parser, "e");

    parser = PegParser.sequence(PegParser.literal("aba"), PegParser.literal("cus"));
    assertParse(parser, "abacus");
    assertWontParse(parser, "aba");
    assertWontParse(parser, "cus");
    assertWontParse(parser, "abacusaba");

    parser = PegParser.repeatPlus(PegParser.literal("a"));
    assertParse(parser, "a");
    assertParse(parser, "aa");
    assertParse(parser, "aaaaaaaaaaaaaaaaaaaaaa");
    assertWontParse(parser, "b");
    assertWontParse(parser, "");

    parser = PegParser.repeatStar(PegParser.literal("a"));
    assertParse(parser, "a");
    assertParse(parser, "aa");
    assertParse(parser, "aaaaaaaaaaaaaaaaaaaaaa");
    assertParse(parser, "");
    assertWontParse(parser, "b");

    parser = PegParser.list(PegParser.literal("a"), PegParser.literal(","));
    assertParse(parser, "a");
    assertParse(parser, "a,a");
    assertParse(parser, "a,a,a,a");
    assertWontParse(parser, "");
    assertWontParse(parser, "b");
    assertWontParse(parser, "a,");
    assertWontParse(parser, "a,b");

    PegParser.Resolvable resolvable = PegParser.call();
    parser = resolvable;
    resolvable.resolve(PegParser.digits());
    assertParse(parser, "1");
    assertWontParse(parser, "");
    assertWontParse(parser, "a");

    parser = PegParser.digits().asList();
    assertTrue(parser.parse(PegParser.input("123")).object instanceof List);

    parser = PegParser.digits().make(Bean.class, "a");
    bean = (Bean) parser.parse(PegParser.input("1234")).object;
    assertEquals("1234", bean.a);

    parser = PegParser.digits().make(Bean.class, "a").set("b", "woops");
    bean = (Bean) parser.parse(PegParser.input("1234")).object;
    assertEquals("1234", bean.a);
    assertEquals("woops", bean.b);

    parser = PegParser.digits().make(Bean.class, "a").set("missing", "123");
    try {
      parser.parse(PegParser.input("1234"));
      fail();
    } catch (RuntimeException ignored) {
    }

    parser = PegParser.digits().asString();
    parser.parse(PegParser.input("abc"));

    parser = PegParser.digits().pick(100);
    parser.parse(PegParser.input("abc"));

    parser = PegParser.digits();
    assertEquals("123", parser.parse(PegParser.input("123")).toString());

    parser = PegParser.optional(PegParser.digits());
    assertEquals("[123]", parser.parse(PegParser.input("123")).toString());

  }

  private void assertWontParse(PegParser parser, String input) {
    Input input1 = PegParser.input(input);
    Output result = parser.parse(input1);
    assertTrue(PegParser.FAILED == result || input1.position != input.length());
  }

  private void assertParse(PegParser parser, String input) {
    Input input1 = PegParser.input(input);
    assertTrue(PegParser.FAILED != parser.parse(input1));
  }

  public static class Bean {

    public String a;
    public String b;
    public String c;
  }
}
