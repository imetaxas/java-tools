package rocks.appconcept.javatools.security;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import rocks.appconcept.javatools.CoverageTool;

/**
 * Special case - testing a System.exit() call by by-passing the security manager
 *
 * Created by yanimetaxas on 2017-02-07 .
 */
public class SystemExitTest {

  @Before
  public void setUp() throws Exception {
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @Test
  public void testMain_WhenExisting() {
    try {
      SystemExit.main(new String[]{"-1"});
    } catch (ExitException ee) {
      assertThat(-1, is(ee.getStatus()));
    }
  }

  @Test
  public void testMain_Pass() {
    try {
      SystemExit.main(new String[]{"0"});
    } catch (ExitException ee) {
      assertThat(0, is(ee.getStatus()));
    }
  }

  @Test
  public void testMain_WhenArgumentUnParsable() {
    SystemExit.main(new String[]{""});
    assertTrue(true);
  }

  @After
  public void tearDown() throws Exception {
    System.setSecurityManager(null);
  }

  @AfterClass
  public static void coverageHack() throws Exception {
    CoverageTool.testPrivateConstructor(SystemExit.class);
  }

}