package rocks.appconcept.javatools.parser;

import junit.framework.TestCase;
import rocks.appconcept.javatools.parser.peg.AstQuotedString;

/**
 * @author yanimetaxas
 */
public class ExpressionParserTest extends TestCase {

  public interface Node {

    int getValue();
  }

  public static class Addition implements Node {

    public Node left;
    public Node right;

    public int getValue() {
      return left.getValue() + right.getValue();
    }
  }

  public static class Multiplication implements Node {

    public Node left;
    public Node right;

    public int getValue() {
      return left.getValue() * right.getValue();
    }
  }

  public static class Primary implements Node {

    public String value;

    public int getValue() {
      return Integer.parseInt(value);
    }
  }

  public PegParser makeExpressionParser() {
    PegParser.Resolvable temp, temp2;

    PegParser integer = PegParser
        .digits()
        .make(Primary.class, "value");

    PegParser variable = PegParser
        .repeatPlus(PegParser.character(Character::isLetter))
        .make(Primary.class)
        .set("value", "0");

    PegParser lookup = PegParser
        .sequence(variable, PegParser.literal("["), new AstQuotedString('"'), PegParser.literal("]"))
        .make(Primary.class).set("value", "666");

    PegParser primary = PegParser
        .options(integer, lookup, variable, PegParser.sequence(PegParser.literal("("), temp2 = PegParser.call(), PegParser.literal(")"))
            .pick(1));

    PegParser multiplicative = PegParser
        .options(PegParser.sequence(primary, PegParser.literal("*"), temp = PegParser.call())
        .make(Multiplication.class, "left", null, "right"), primary);

    temp.resolve(multiplicative);

    PegParser additive = PegParser
        .options(PegParser.sequence(multiplicative, PegParser.literal("+"), temp = PegParser.call())
        .make(Addition.class, "left", null, "right"), multiplicative);

    temp.resolve(additive);
    temp2.resolve(additive);

    return additive;
  }


  public void testExpressionParser() throws Exception {
    PegParser parser = makeExpressionParser();

    assertEquals(0, ((Node) parser.parse(PegParser.input("(2+2*3)*CAR")).object).getValue());
    assertEquals(15378, ((Node) parser.parse(PegParser.input("test+12+23*window[\"test\"]+(12*3+12)")).object).getValue());
    assertEquals(123, ((Node) parser.parse(PegParser.input("123")).object).getValue());
    assertEquals(444, ((Node) parser.parse(PegParser.input("123+321")).object).getValue());
    assertEquals(6, ((Node) parser.parse(PegParser.input("2*3")).object).getValue());
    assertEquals(6, ((Node) parser.parse(PegParser.input("2*(1+2)")).object).getValue());
    assertEquals(6, ((Node) parser.parse(PegParser.input("(1+1)*3")).object).getValue());
    assertEquals(8, ((Node) parser.parse(PegParser.input("2*3+2")).object).getValue());
    assertEquals(8, ((Node) parser.parse(PegParser.input("2+2*3")).object).getValue());
    assertEquals(10000 + 12 * (13 + 14), ((Node) parser.parse(PegParser.input("10000+12*(13+14)")).object).getValue());
  }
}
