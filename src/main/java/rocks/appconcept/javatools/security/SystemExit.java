package rocks.appconcept.javatools.security;

/**
 * Created by yanimetaxas on 2017-02-07.
 */
public class SystemExit {

  private SystemExit() {
  }

  public static void main(String[] args) {
    try {
      System.exit(Integer.parseInt(args[0]));
    } catch (ExitException e) {
      if(e.getStatus() == -1) {
        throw e;
      }
    } catch (Exception e){
    }
  }
}
