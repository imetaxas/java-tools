package rocks.appconcept.javatools.security;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import rocks.appconcept.javatools.CoverageTool;

/**
 * Special case - testing a System.exit() call by by-passing the security manager
 *
 * Created by yanimetaxas on 2017-02-07.
 */
public class SystemExitTest {

  @Before
  public void setUp() throws Exception {
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @Test
  public void main() {
    try {
      SystemExit.main(new String[]{});
    } catch (ExitException ee) {
      assertThat(0, is(ee.getStatus()));
    }
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