package rocks.appconcept.javatools.xml;

public interface ReductionHandler {

  void setup();

  void tearDown();

  Object handleException(Throwable exception, XmlReducerNode node);
}
