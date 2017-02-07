package rocks.appconcept.javatools.security;

import java.security.Permission;

/**
 * Created by yanimetaxas on 2017-02-07.
 */
public class NoExitSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    // allow anything.
  }

  @Override
  public void checkPermission(Permission perm, Object context) {
    // allow anything.
  }

  @Override
  public void checkExit(int status) {
    super.checkExit(status);
    throw new ExitException(status);
  }
}
