package rocks.appconcept.javatools.gzip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 */
public class Gzip {

  public static byte[] compress(byte[] content) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
      gzipOutputStream.write(content);
      gzipOutputStream.flush();
      gzipOutputStream.close();
      byteArrayOutputStream.close();
      return byteArrayOutputStream.toByteArray();
    } catch (Throwable e) {
      return null;
    }
  }

  public static byte[] decompress(byte[] compressedContent) {
    try {
      int len = getGzipUncompressedLength(compressedContent);
      byte[] content = new byte[len];
      ByteArrayInputStream bais = new ByteArrayInputStream(compressedContent);
      GZIPInputStream gzin = new GZIPInputStream(bais);
      int total = 0;
      while (total < len) {
        int read = gzin.read(content, total, len - total);
          if (read == -1) {
              break;
          }
        total += read;
      }
      gzin.close();
      bais.close();
      return content;
    } catch (Throwable e) {
      return null;
    }
  }

  private static int getGzipUncompressedLength(byte[] bytes) {
    int offset = bytes.length - 4;
    return ((((int) bytes[offset]) & 0xff)) +
        ((((int) bytes[offset + 1]) & 0xff) << 8) +
        ((((int) bytes[offset + 2]) & 0xff) << 16) +
        ((((int) bytes[offset + 3]) & 0xff) << 24);
  }

}
