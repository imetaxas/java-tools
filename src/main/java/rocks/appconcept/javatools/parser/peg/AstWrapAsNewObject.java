package rocks.appconcept.javatools.parser.peg;

import java.lang.reflect.Field;
import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstWrapAsNewObject extends AstBase {

  private final PegParser inner;
  private final Class<?> type;
  private final String[] propertyNames;
  private final Field[] fields;

  public AstWrapAsNewObject(PegParser inner, Class<?> type, String... propertyNames) {
    this.inner = inner;
    this.type = type;
    this.propertyNames = propertyNames;
    this.fields = new Field[propertyNames.length];
  }

  @Override
  public Output parse(Input input) {
    Output parse = inner.parse(input);
    if (parse != PegParser.FAILED) {

      try {
        Object obj = type.newInstance();
        for (int i = 0; i < propertyNames.length; i++) {
          String prop = propertyNames[i];
          if (prop != null) {
            Object val = parse.list == null ? parse.object : parse.list.get(i).object;
            if (fields[i] == null) {
              fields[i] = type.getDeclaredField(prop);
              fields[i].setAccessible(true);
            }
            fields[i].set(obj, val);
          }
        }
        return new Output(obj);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return parse;
  }
}
