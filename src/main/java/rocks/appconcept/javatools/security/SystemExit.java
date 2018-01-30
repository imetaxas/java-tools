package rocks.appconcept.javatools.security;

/**
 * Created by yanimetaxas on 2017-02-07.
 */
public class SystemExit {

  private SystemExit() {
  }

  public static void main(String[] args) {
    try {
      System.exit(0);
    } catch (ExitException e) {
      e.printStackTrace();
    }
  }
}
