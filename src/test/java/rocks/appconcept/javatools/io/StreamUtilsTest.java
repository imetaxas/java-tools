package rocks.appconcept.javatools.io;

import rocks.appconcept.javatools.CoverageTool;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author yanimetaxas
 */
public class StreamUtilsTest extends TestCase {

    public void testStatic() throws Exception {
        CoverageTool.callPrivateConstructor(StreamUtils.class);
    }

    public void testCapture() throws Exception {

        byte [] bytes = StreamUtils.captureBytes(stream->{
            stream.write(new byte[]{'A'});
            stream.write(new byte[]{'B', 'C'});
            stream.write(new byte[]{'D'});
        });
        assertEquals("ABCD", new String(bytes, StandardCharsets.UTF_8));

        //noinspection Convert2Lambda
        bytes = StreamUtils.captureBytes(new StreamUtils.Capturer() {
            @Override
            public void accept(ByteArrayOutputStream stream) throws IOException {
                stream.write(new byte[]{'A'});
                stream.write(new byte[]{'B', 'C'});
                stream.write(new byte[]{'D'});
            }
        });
        assertEquals("ABCD", new String(bytes, StandardCharsets.UTF_8));
    }

    public void testVoidStream() throws Exception {
        OutputStream os = new StreamUtils.VoidOutputStream();
        os.write(0);
        os.write(new byte[1]);
        os.write(new byte[1], 0, 1);
        os.flush();
        os.close();
    }
}