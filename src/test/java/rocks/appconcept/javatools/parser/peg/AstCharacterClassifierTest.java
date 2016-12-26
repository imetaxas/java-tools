package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;
import junit.framework.TestCase;

/**
 * @author yanimetaxas
 */
public class AstCharacterClassifierTest extends TestCase {

    public void testLetters() throws Exception {
        PegParser p = PegParser.repeatPlus(PegParser.character(Character::isAlphabetic));
        assertTrue(parse(p, "aåäö"));
        assertTrue(parse(p, "\ufeb1\ufeb2\ufeb3"));
        assertFalse(parse(p, "0"));
        assertFalse(parse(p, "."));
        assertFalse(parse(p, ","));
        assertFalse(parse(p, "-"));
        assertFalse(parse(p, "+"));
    }

    private boolean parse(PegParser p, String text) {
        //System.out.println(text);
        Input i = new Input(text, 0);
        Output parse = p.parse(i);
        if (parse == PegParser.FAILED || i.position != text.length()) {
            //System.err.println("Failed to parse: " + text.substring(i.position));
            return false;
        }
        return true;
    }
}