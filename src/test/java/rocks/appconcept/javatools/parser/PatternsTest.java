package rocks.appconcept.javatools.parser;

import rocks.appconcept.javatools.CoverageTool;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * @author yanimetaxas
 */
public class PatternsTest extends TestCase {

    public void testStatic() throws Exception {
        CoverageTool.callPrivateConstructor(Patterns.class);
    }

    public void testAllMatches() throws Exception {
        Pattern pattern = Pattern.compile("(\\d+)");
        List<String> matches = new ArrayList<>();
        for (MatchResult matchResult : Patterns.allMatches(pattern, "1,2,3,4,10")) {
            matches.add(matchResult.group(1));
        }
        assertEquals(Arrays.asList("1","2","3","4","10"), matches);

        try {
            Patterns.allMatches(pattern, "1,2,3,4,10").iterator().remove();
            fail();
        } catch (UnsupportedOperationException ignored) {
        }

        try {
            Patterns.allMatches(pattern, "").iterator().next();
            fail();
        } catch (NoSuchElementException ignored) {
        }
    }

    public void testReplaceAll() throws Exception {
        Pattern pattern = Pattern.compile("(\\d+)");
        assertEquals("[1],[2],[3],[4],[5]", Patterns.replaceAll(pattern, "1,2,3,4,5", m-> "[" + m.group(0) + "]"));
        assertEquals("babbel[1],[2],[3],[4],[5]dravel", Patterns.replaceAll(pattern, "babbel1,2,3,4,5dravel", m-> "[" + m.group(0) + "]"));
        //noinspection Convert2Lambda
        assertEquals("babbel[1],[2],[3],[4],[5]dravel", Patterns.replaceAll(pattern, "babbel1,2,3,4,5dravel", new Patterns.Replacer() {
            @Override
            public String replace(MatchResult result) {
                return "[" + result.group() + "]";
            }
        }));
    }

    public void testReplaceToStringBuilder() throws Exception {
        Pattern pattern = Pattern.compile("(\\d+)");
        StringBuilder out = new StringBuilder("apa");
        Patterns.replaceAll(pattern, "[1],[2]", out, r->Integer.toString(Integer.parseInt(r.group(1))*20));
        assertEquals("apa[20],[40]", out.toString());
    }
}
