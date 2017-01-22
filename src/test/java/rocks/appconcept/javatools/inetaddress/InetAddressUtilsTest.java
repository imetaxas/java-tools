package rocks.appconcept.javatools.inetaddress;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by imeta on 22-Jan-17.
 */
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({InetAddress.class, InetAddressUtils.class})
public class InetAddressUtilsTest {

  @Test
  public void testInetAddressUtilsConstructor() throws Exception {
    Constructor<InetAddressUtils> constructor = InetAddressUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    Assert.assertNotNull(constructor.newInstance());
  }

  @Test
  public void testGetIpAddress() throws Exception {
    byte[] bytes = InetAddress.getLocalHost().getAddress();
    Assert.assertArrayEquals(bytes, InetAddressUtils.getIpAddress());
  }

  @Test
  public void testGetLocalHost() throws Exception {
    String name = InetAddress.getLocalHost().getHostName();
    Assert.assertEquals(name, InetAddressUtils.getHostName());
  }

  @Test
  public void testGetIpAddressWithUnkownHost() throws Exception {
    PowerMockito.mockStatic(InetAddress.class);
    PowerMockito.when(InetAddress.getLocalHost()).thenThrow(new UnknownHostException());
    Assert.assertArrayEquals(new byte[]{0, 0, 0, 0}, InetAddressUtils.getIpAddress());
    Assert.assertEquals("localhost", InetAddressUtils.getHostName());
  }
}