package rocks.appconcept.javatools.template;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author yanimetaxas
 */
public class HtmlTemplateTest extends TestCase {

    public void testSimple() throws Exception {
        HtmlTemplate template = new HtmlTemplate("hello $world! Variable: $variable", Collections.singletonMap("world", "COOL!"));
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        template.renderTemplate(Collections.singletonMap("variable", "NERD!"), writer);
        assertEquals("hello COOL!! Variable: NERD!", new String(writer.toByteArray(), UTF_8));
    }

    public void testComment() throws Exception {
        HtmlTemplate template = new HtmlTemplate("This #* is a multi\nline\ncomment\n${variable}*#here.", null);
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        template.renderTemplate(Collections.singletonMap("variable", "NERD!"), writer);
        assertEquals("This here.", new String(writer.toByteArray(), UTF_8));
    }

    public void testWrapped() throws Exception {
        HtmlTemplate template = new HtmlTemplate("hello ${world}! Variable: ${variable}", Collections.singletonMap("world", "COOL!"));
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        template.renderTemplate(Collections.singletonMap("variable", "NERD!"), writer);
        assertEquals("hello COOL!! Variable: NERD!", new String(writer.toByteArray(), UTF_8));
    }

    public void testConditionals() throws Exception {
        HtmlTemplate template = new HtmlTemplate("hello#if($x) spunk#end.", null);
        ByteArrayOutputStream writer;

        writer = new ByteArrayOutputStream();
        template.renderTemplate(null, writer);
        assertEquals("hello.", new String(writer.toByteArray(), UTF_8));

        writer = new ByteArrayOutputStream();
        template.renderTemplate(Collections.singletonMap("x", "1"), writer);
        assertEquals("hello spunk.", new String(writer.toByteArray(), UTF_8));
    }

    public void testNestedConditionals() throws Exception {
        HtmlTemplate template = new HtmlTemplate("#if($a)A but how about#if($b) B too#end?#end", null);
        ByteArrayOutputStream writer;

        writer = new ByteArrayOutputStream();
        template.renderTemplate(null, writer);
        assertEquals("", new String(writer.toByteArray(), UTF_8));

        writer = new ByteArrayOutputStream();
        template.renderTemplate(Collections.singletonMap("a", "1"), writer);
        assertEquals("A but how about?", new String(writer.toByteArray(), UTF_8));

        writer = new ByteArrayOutputStream();
        Map<String, String> map = new HashMap<>();
        map.put("a", "a");
        map.put("b", "b");
        template.renderTemplate(map, writer);
        assertEquals("A but how about B too?", new String(writer.toByteArray(), UTF_8));
    }

    public void testPresetConditionals() throws Exception {
        HtmlTemplate template;
        ByteArrayOutputStream writer;

        template = new HtmlTemplate("#if($a)a conditional#end", null);
        writer = new ByteArrayOutputStream();
        template.renderTemplate(null, writer);
        assertEquals("", new String(writer.toByteArray(), UTF_8));

        template = new HtmlTemplate("#if($a)a conditional#end", null);
        writer = new ByteArrayOutputStream();
        template.renderTemplate(Collections.singletonMap("a", "1"), writer);
        assertEquals("a conditional", new String(writer.toByteArray(), UTF_8));

        template = new HtmlTemplate("#if($a)a conditional#end", Collections.singletonMap("a", (String) null));
        writer = new ByteArrayOutputStream();
        template.renderTemplate(Collections.singletonMap("a", "1"), writer);
        assertEquals("conditional should be precompiled away", "", new String(writer.toByteArray(), UTF_8));

        template = new HtmlTemplate("#if($a)a conditional#end", Collections.singletonMap("a", "1"));
        writer = new ByteArrayOutputStream();
        template.renderTemplate(null, writer);
        assertEquals("conditional should be precompiled on", "a conditional", new String(writer.toByteArray(), UTF_8));
    }


    public void testUnbalancedConditional() throws Exception {
        try {
            new HtmlTemplate("#if($a)A but how about#if($b) B too#end?", null);
            fail();
        } catch (Exception ignored) {
        }
    }

    public void testNonGreedyComments() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        new HtmlTemplate("#* this is a comment *#this is some text#* another comment *#", null).renderTemplate(null, stream);
        assertEquals("this is some text", new String(stream.toByteArray(), UTF_8));
    }

    public void testConditionalsWithVariable() throws Exception {
        HtmlTemplate template = new HtmlTemplate("hello#if($x) $x#end.", null);
        ByteArrayOutputStream writer;

        writer = new ByteArrayOutputStream();
        template.renderTemplate(null, writer);
        assertEquals("hello.", new String(writer.toByteArray(), UTF_8));

        writer = new ByteArrayOutputStream();
        template.renderTemplate(Collections.singletonMap("x", "spunkelicious"), writer);
        assertEquals("hello spunkelicious.", new String(writer.toByteArray(), UTF_8));
    }

    public void testInclude() throws Exception {

        HtmlTemplate template = new HtmlTemplate("hello #include(\"test.vm\")", null);
        HtmlTemplate include = new HtmlTemplate("world ($x)", null);
        template.setIncludableTemplates(Collections.singletonMap("test.vm", include));
        ByteArrayOutputStream writer;

        writer = new ByteArrayOutputStream();
        template.renderTemplate(Collections.singletonMap("x", "spunkelicious"), writer);
        assertEquals("hello world (spunkelicious)", new String(writer.toByteArray(), UTF_8));
    }
}
