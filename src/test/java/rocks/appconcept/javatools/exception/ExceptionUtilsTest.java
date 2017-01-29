package rocks.appconcept.javatools.exception;

import java.lang.reflect.Constructor;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by imeta on 22-Jan-17.
 */
public class ExceptionUtilsTest {

  @Test
  public void testExceptionUtilsConstructor() throws Exception {
    Constructor<ExceptionUtils> constructor = ExceptionUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    Assert.assertNotNull(constructor.newInstance());
  }

  @Test(expected = ThreadDeath.class)
  public void testThreadDeathThrown() {
    ExceptionUtils.safelyIgnoreException(new ThreadDeath());
  }

  @Test(expected = OutOfMemoryError.class)
  public void testOutOfMemoryErrorThrown() {
    ExceptionUtils.safelyIgnoreException(new OutOfMemoryError());
  }

  @Test
  public void testIgnoredExceptions() {
    ExceptionUtils.safelyIgnoreException(new IllegalArgumentException());
  }
}