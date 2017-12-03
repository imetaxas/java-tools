package rocks.appconcept.javatools.gzip;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.junit.AfterClass;
import org.junit.Test;
import rocks.appconcept.javatools.CoverageTool;
import rocks.appconcept.javatools.security.SystemExit;

/**
 * Created by imeta on 26-Dec-16.
 */
public class GzipTest {

  private byte rawData[] = {
      0x50, 0x4B, 0x03, 0x04, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xF0, 0x63,
      (byte) 0x90, 0x46, (byte) 0x8B, 0x73, (byte) 0x95, (byte) 0xAC, 0x09, 0x00, 0x00, 0x00, 0x09,
      0x00,
      0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x2E,
      0x74, 0x78, 0x74, 0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x7A, 0x69, 0x70,
      0x50, 0x4B, 0x01, 0x02, 0x3F, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00,
      (byte) 0xF0, 0x63, (byte) 0x90, 0x46, (byte) 0x8B, 0x73, (byte) 0x95, (byte) 0xAC, 0x09, 0x00,
      0x00, 0x00,
      0x09, 0x00, 0x00, 0x00, 0x09, 0x00, 0x24, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x68, 0x65,
      0x6C, 0x6C, 0x6F, 0x2E, 0x74, 0x78, 0x74, 0x0A, 0x00, 0x20, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x01, 0x00, 0x18, 0x00, 0x3E, 0x47, 0x60, (byte) 0x80, 0x30,
      0x78, (byte) 0xD0, 0x01, 0x66, 0x1F, 0x5B, 0x2E, 0x30, 0x78, (byte) 0xD0, 0x01, 0x66,
      0x1F, 0x5B, 0x2E, 0x30, 0x78, (byte) 0xD0, 0x01, 0x50, 0x4B, 0x05, 0x06, 0x00,
      0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x5B, 0x00, 0x00, 0x00, 0x30,
      0x00, 0x00, 0x00, 0x00, 0x00
  };

  @Test
  public void compress() throws Exception {
    byte[] compressed = Gzip.compress(rawData);
    byte[] decompressed = Gzip.decompress(compressed);

    assertEquals(new String(rawData, StandardCharsets.UTF_8),
        new String(decompressed, StandardCharsets.UTF_8));
  }

  @AfterClass
  public static void coverageHack() throws Exception {
    CoverageTool.testPrivateConstructor(Gzip.class);
  }

}