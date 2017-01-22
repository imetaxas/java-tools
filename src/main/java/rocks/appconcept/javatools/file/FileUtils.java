package rocks.appconcept.javatools.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * @author yanimetaxas
 */
public class FileUtils {

  private FileUtils() {
  }

  /**
   * Copies the contents of the input stream into the output stream.
   *
   * @param is The input stream to read from.
   * @param os The output stream to write to.
   * @return The total number of bytes copied.
   * @throws IOException If one of the streams couldn't be read/written from/to.
   */
  public static int copyStream(InputStream is, OutputStream os) throws IOException {
    byte[] buf = new byte[1024 * 8];
    int totalRead = 0;
    int read = is.read(buf);
    while (read >= 0) {
      totalRead += read;
      if (os != null) {
        os.write(buf, 0, read);
      }
      read = is.read(buf);
    }
    return totalRead;
  }

  /**
   * Reads the full contents of the source file into a byte array.
   *
   * @param sourceFile The source file to read from.
   * @return The bytes contained in the file.
   * @throws IOException If the file couldn't be read from.
   */
  public static byte[] readFully(File sourceFile) throws IOException {
    byte[] buf = new byte[(int) sourceFile.length()];
    InputStream is = null;
    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(sourceFile);
      is = new BufferedInputStream(fileInputStream);
      int remaining = (int) sourceFile.length();
      while (remaining > 0) {
        int read = is.read(buf, buf.length - remaining, Math.min(1024 * 1024, remaining));
        remaining -= read;
        if (read < 0) {
          break;
        }
      }
    } finally {
      if (is != null) {
        is.close();
      }
      if (fileInputStream != null) {
        fileInputStream.close();
      }
    }

    return buf;
  }

  /**
   * Reads the full contents of the input stream into a byte array.
   *
   * @param is The input stream to read from.
   * @return The bytes contained in the stream.
   * @throws IOException If the stream couldn't be read from.
   */
  public static byte[] readFully(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024 * 8];
    int read = is.read(buf);
    while (read > 0) {
      baos.write(buf, 0, read);
      read = is.read(buf);
    }

    return baos.toByteArray();
  }

  /**
   * Reads the full content of the reader into a string. Line breaks will always be converted into
   * '\n'.
   *
   * @param reader The reader.
   * @return The read string.
   * @throws IOException If the stream couldn't be read from.
   */
  public static String readFully(Reader reader) throws IOException {
    BufferedReader br = new BufferedReader(reader);
    StringBuilder sb = new StringBuilder();
    String line = br.readLine();
    while (line != null) {
      sb.append(line);
      line = br.readLine();
      if (line != null) {
        sb.append("\n");
      }
    }
    br.close();
    return sb.toString();
  }

  /**
   * Writes the full contents of data into the target file.
   *
   * @param targetFile The target file.
   * @param data The data to write.
   * @throws IOException If the stream couldn't be read or the file couldn't be written to.
   */
  public static void writeFully(File targetFile, byte[] data) throws IOException {
    OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile));
    os.write(data);
    os.flush();
    os.close();
  }

  /**
   * Creates a reader with UTF-8 encoding from an input stream.
   *
   * @param is The input stream.
   * @return A reader with UTF-8 encoding.
   */
  public static Reader readerFromStream(InputStream is) {
    return new InputStreamReader(is, StandardCharsets.UTF_8);
  }


}
