package rocks.appconcept.javatools.binary;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author yanimetaxas
 */
public class Hash {

  private Hash() {
  }

  private static final char[] hexChars = "0123456789abcdef".toCharArray();

  public static String hex(byte[] data) {
    char[] output = new char[data.length * 2];
    for (int i = 0; i < data.length; i++) {
      int b = data[i] & 0xff;
      output[i * 2] = hexChars[b >>> 4];
      output[i * 2 + 1] = hexChars[b & 0xf];
    }
    return new String(output);
  }

  public static String sha1hex(byte[] data) throws NoSuchAlgorithmException {
    return hex(MessageDigest.getInstance("SHA-1").digest(data));
  }

  public static String sha256hex(byte[] data) throws NoSuchAlgorithmException {
    return hex(MessageDigest.getInstance("SHA-256").digest(data));
  }

  public static String md5hex(byte[] data) throws NoSuchAlgorithmException {
    return hex(MessageDigest.getInstance("MD5").digest(data));
  }
}
