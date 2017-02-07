package rocks.appconcept.javatools.security;

/**
 * Created by yanimetaxas on 2017-02-07.
 */
public class ExitException extends SecurityException {

  private final int status;

  public ExitException(int status) {
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
