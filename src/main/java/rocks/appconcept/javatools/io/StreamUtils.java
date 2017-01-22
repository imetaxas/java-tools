package rocks.appconcept.javatools.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author yanimetaxas
 */
public class StreamUtils {

  private StreamUtils() {
  }

  public interface Capturer {

    void accept(ByteArrayOutputStream baos) throws IOException;
  }

  public static byte[] captureBytes(Capturer lambda) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    lambda.accept(baos);
    return baos.toByteArray();
  }

  /**
   * OutputStream that doesn't write anything.
   */
  public static class VoidOutputStream extends OutputStream {

    @Override
    public void write(byte[] b) throws IOException {
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    }

    @Override
    public void write(int b) throws IOException {
    }
  }
}
