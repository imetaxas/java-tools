package rocks.appconcept.javatools.inetaddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author yanimetaxas
 */
public class InetAddressUtils {

  private InetAddressUtils() {
  }

  public static InetAddress getLocalHost() throws UnknownHostException {
    return InetAddress.getLocalHost();
  }

  public static byte[] getIpAddress() {
    try {
      return getLocalHost().getAddress();
    } catch (Exception e) {
      System.err.println("Failed to obtain computer's IP address");
      return new byte[]{0, 0, 0, 0};
    }
  }

  public static String getHostName() {
    try {
      return getLocalHost().getHostName();
    } catch (Exception e) {
      System.err.println("Unable to fetch 'hostname'");
      return "localhost";
    }
  }


}
