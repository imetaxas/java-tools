package rocks.appconcept.javatools.exception;

/**
 * Created by imeta on 22-Jan-17.
 */
public class ExceptionUtils {

  private ExceptionUtils() {
    super();
  }


  /**
   * Safely Ignore a Throwable or rethrow if it is a Throwable that should not be ignored.
   * @param t
   */
  public static void safelyIgnoreException(Throwable t)
  {
    if (t instanceof ThreadDeath)
    {
      throw (ThreadDeath) t;
    }

    if (t instanceof OutOfMemoryError)
    {
      throw (OutOfMemoryError) t;
    }

  }
}
