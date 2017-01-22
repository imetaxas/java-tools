package rocks.appconcept.javatools.json;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import junit.framework.TestCase;

/**
 * @author yanimetaxas
 */
public class JSONStreamTest extends TestCase {

  public void testErrorHandling() {

    try {
      new JSONStream().value("a").value("b");
      fail();
    } catch (JSONStream.JSONException ignored) {
    }
    try {
      new JSONStream().value("a").list();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }
    try {
      new JSONStream().value("a").object();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }
    try {
      new JSONStream().value("a").verbatim("null");
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().end();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().value("prop", "val");
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().value("prop", true);
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().value("prop", 12);
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().list("prop").endlist();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().object("prop").endobject();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().endlist();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().endobject();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().end();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().object().value("val");
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().object().value(false);
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().object().value(12);
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().object().list();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

    try {
      new JSONStream().object().object();
      fail();
    } catch (JSONStream.JSONException ignored) {
    }

  }

  public void testVerbatim() {
    {
      JSONStream st = new JSONStream();
      st.verbatim("[1]");
      assertEquals("[1]", st.toString());
    }
    {
      try {
        JSONStream st = new JSONStream();
        st.verbatim("prop", "[1]");
        fail();
      } catch (JSONStream.JSONException ignored) {
      }
    }
    {
      JSONStream st = new JSONStream();
      st.object();
      st.verbatim("prop", "[1]");
      st.value("prop2", "hej");
      st.end();
      assertEquals("{\"prop\":[1],\"prop2\":\"hej\"}", st.toString());
    }
  }

  public void testList() {
    {
      JSONStream st = new JSONStream();
      st.list().value("ninja").value("power").list().value("grok").endlist().endlist();
      assertEquals("[\"ninja\",\"power\",[\"grok\"]]", st.toJSON());
    }

    {
      JSONStream st = new JSONStream();
      st.list().list().list().value("apa").endlist().list().value("ninja").endlist().endlist()
          .endlist();
      assertEquals("[[[\"apa\"],[\"ninja\"]]]", st.toJSON());
    }

    {
      JSONStream st = new JSONStream();
      st.list().list().value("apa").endlist().value("ninja").endlist();
      assertEquals("[[\"apa\"],\"ninja\"]", st.toJSON());
    }

    {
      JSONStream st = new JSONStream();
      st.list().value("ninja").list().value("apa").endlist().endlist();
      assertEquals("[\"ninja\",[\"apa\"]]", st.toJSON());
    }
  }

  public void testObject() {
    {
      JSONStream st = new JSONStream();
      st.object().value("prop", "val").value("prop2", "val2").endobject();
      assertEquals("{\"prop\":\"val\",\"prop2\":\"val2\"}", st.toJSON());
    }

    {
      JSONStream st = new JSONStream();
      st.object().object("prop").value("prop2", "val2").endobject().endobject();
      assertEquals("{\"prop\":{\"prop2\":\"val2\"}}", st.toJSON());
    }

    {
      JSONStream st = new JSONStream();
      st.object().object("prop").value("prop2", "val2").endall();
      assertEquals("{\"prop\":{\"prop2\":\"val2\"}}", st.toJSON());
    }
  }

  public void testComplex() {
    JSONStream st = new JSONStream();

    st.object().value("version", 1).list("quotes");
    for (int i = 0; i < 3; i++) {
      st.object().value("id", i).value("owner", "user " + i).value("odd", (i % 2) == 1).end();
    }
    st.endall();

    assertEquals(
        "{\"version\":1,\"quotes\":[{\"id\":0,\"owner\":\"user 0\",\"odd\":false},{\"id\":1,\"owner\":\"user 1\",\"odd\":true},{\"id\":2,\"owner\":\"user 2\",\"odd\":false}]}",
        st.toJSON());

    JSONObject parsed = JSONObject.parse(st.toString());
    assertEquals(1, (int) parsed.get("version").asInteger());
    assertEquals(3, parsed.get("quotes").length());
    int i = 0;
    for (JSONObject quote : parsed.get("quotes").values()) {
      assertEquals(i, (int) quote.get("id").asInteger());
      assertEquals("user " + i, quote.get("owner").asString());
      assertEquals((i % 2) == 1, quote.get("odd").asBoolean());
      ++i;
    }

    String rewritten = parsed.toJSON();
    // assertEquals(st.toString(), rewritten); // Not true because map order is not preserved.

    parsed = JSONObject.parse(rewritten);
    assertEquals(1, (int) parsed.get("version").asInteger());
    assertEquals(3, parsed.get("quotes").length());
    i = 0;
    for (JSONObject quote : parsed.get("quotes").values()) {
      assertEquals(i, (int) quote.get("id").asInteger());
      assertEquals("user " + i, quote.get("owner").asString());
      assertEquals((i % 2) == 1, quote.get("odd").asBoolean());
      ++i;
    }

  }

  public void testUnicode() {
    JSONStream json = new JSONStream();
    json.list();
    json.value("åäöÅÄÖﺲﻚﻱ\u0009");
    json.endlist();
    assertEquals("[\"åäöÅÄÖﺲﻚﻱ\\t\"]", json.toJSON());

    JSONObject parsed = JSONObject.parse(json.toString());
    assertEquals("åäöÅÄÖﺲﻚﻱ\t", parsed.get(0).asString());

    String rewritten = parsed.toJSON();
    assertEquals(json.toString(), rewritten);
    parsed = JSONObject.parse(rewritten);
    assertEquals("åäöÅÄÖﺲﻚﻱ\t", parsed.get(0).asString());
  }

  public void testSingleValue() {
    assertEquals("\"a\"", new JSONStream().value("a").toString());
    assertEquals("12", new JSONStream().value(12).toString());
    assertEquals("true", new JSONStream().value(true).toString());
    assertEquals("false", new JSONStream().value(false).toString());
    assertEquals("null", new JSONStream().value(null).toString());
  }

  public void testAsJavascript() throws Exception {

    String str = "th\u0002is\u2029breaks\u2028</script><script>alert('hello');</script>";
    String s = new JSONStream().object().value("a", str).endobject().toJSON();
    ScriptEngine js = new ScriptEngineManager().getEngineByName("javascript");
    Bindings bindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
    bindings.put("stdout", System.out);
    Object eval = js.eval("(" + s + ").a");
    assertEquals("0x2029 and 0x2028 must be properly escaped", str, eval);
  }

    /*public void testPerf() {

        int REP = 10000;
        long start = System.currentTimeMillis();
            for (int i=0; i<REP; i++) {
                JSONStream stream = new JSONStream();
                stream.list();
                for (int j=0; j<30; j++) {
                    stream.object();
                    stream.value("pro1111111111111111111111p", "abc123222222222222222222222222222222222");
                    stream.list("prop2xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                    for (int x=0; x<10; x++) {
                        stream.value("apaxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                    }
                    stream.endlist();
                    stream.endobject();
                }
                stream.endlist();
                String res = stream.toJSON();
                if (20101 != res.length()) throw new RuntimeException("Bad output");
            }
        long end = System.currentTimeMillis();
        System.out.println( (end-start)/(double) REP);
    }*/


  public void testThreadSafe() {
    final AtomicBoolean fail = new AtomicBoolean(false);
    Thread[] thread = new Thread[10];
    for (int i = 0, threadLength = thread.length; i < threadLength; i++) {
      thread[i] = new Thread(Integer.toString(i)) {
        public void run() {
          String me = Thread.currentThread().getName();
          for (int jj = 0; jj < 1000; jj++) {
            JSONStream json = new JSONStream();
            json.list();
            json.value(me);
            json.endlist();
            String val = json.toJSON();
            if (!val.equals("[\"" + me + "\"]")) {
              fail.set(true);
            }
          }
        }
      };
      thread[i].start();
    }
    for (Thread thread1 : thread) {
      try {
        thread1.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    if (fail.get()) {
      fail("Class is not threadsafe");
    }
  }
}
