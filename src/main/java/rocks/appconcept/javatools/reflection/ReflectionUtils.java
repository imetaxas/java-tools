package rocks.appconcept.javatools.reflection;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import rocks.appconcept.javatools.exception.ExceptionUtils;

/**
 * Created by imeta on 22-Jan-17.
 */
public class ReflectionUtils {

  private static final Map<Class, Collection<Field>> _reflectedFields = new ConcurrentHashMap<>();

  private ReflectionUtils() {
    super();
  }

  /**
   * Determine if the passed in class (classToCheck) has the annotation (annoClass) on itself,
   * any of its super classes, any of it's interfaces, or any of it's super interfaces.
   * This is a exhaustive check throughout the complete inheritance hierarchy.
   *
   * @return the Annotation if found, null otherwise.
   */
  public static Annotation getClassAnnotation(final Class classToCheck, final Class annoClass) {
    final Set<Class> visited = new HashSet<>();
    final LinkedList<Class> stack = new LinkedList<>();
    stack.add(classToCheck);

    while (!stack.isEmpty()) {
      Class classToChk = stack.pop();
      if (classToChk == null || visited.contains(classToChk)) {
        continue;
      }
      visited.add(classToChk);
      Annotation a = classToChk.getAnnotation(annoClass);
      if (a != null) {
        return a;
      }
      stack.push(classToChk.getSuperclass());
      addInterfaces(classToChk, stack);
    }
    return null;
  }

  private static void addInterfaces(final Class classToCheck, final LinkedList<Class> stack) {
    for (Class interFace : classToCheck.getInterfaces()) {
      stack.push(interFace);
    }
  }

  public static Annotation getMethodAnnotation(final Method method, final Class annoClass) {
    final Set<Class> visited = new HashSet<>();
    final LinkedList<Class> stack = new LinkedList<>();
    stack.add(method.getDeclaringClass());

    while (!stack.isEmpty()) {
      Class classToChk = stack.pop();
      if (classToChk == null || visited.contains(classToChk)) {
        continue;
      }
      visited.add(classToChk);
      Method m = getMethod(classToChk, method.getName(), method.getParameterTypes());
      if (m == null) {
        continue;
      }
      Annotation a = m.getAnnotation(annoClass);
      if (a != null) {
        return a;
      }
      stack.push(classToChk.getSuperclass());
      addInterfaces(method.getDeclaringClass(), stack);
    }
    return null;
  }

  public static Method getMethod(Class c, String method, Class... types) {
    try {
      return c.getMethod(method, types);
    } catch (Exception nse) {
      return null;
    }
  }

  /**
   * Get all non static, non transient, fields of the passed in class, including
   * private fields. Note, the special this$ field is also not returned.  The result
   * is cached in a static ConcurrentHashMap to benefit execution performance.
   *
   * @param c Class instance
   * @return Collection of only the fields in the passed in class that would need further processing
   * (reference fields).  This makes field traversal on a class faster as it does not need to
   * continually process known fields like primitives.
   */
  public static Collection<Field> getDeepDeclaredFields(Class c) {
    if (_reflectedFields.containsKey(c)) {
      return _reflectedFields.get(c);
    }
    Collection<Field> fields = new ArrayList<>();
    Class curr = c;

    while (curr != null) {
      getDeclaredFields(curr, fields);
      curr = curr.getSuperclass();
    }
    _reflectedFields.put(c, fields);
    return fields;
  }

  /**
   * Get all non static, non transient, fields of the passed in class, including
   * private fields. Note, the special this$ field is also not returned.  The
   * resulting fields are stored in a Collection.
   *
   * @param c Class instance that would need further processing (reference fields).  This makes
   * field traversal on a class faster as it does not need to continually process known fields like
   * primitives.
   */
  public static void getDeclaredFields(Class c, Collection<Field> fields) {
    try {
      Field[] local = c.getDeclaredFields();

      for (Field field : local) {
        if (!field.isAccessible()) {
          try {
            field.setAccessible(true);
          } catch (Exception ignored) {
          }
        }

        int modifiers = field.getModifiers();
        if (!Modifier.isStatic(modifiers) &&
            !field.getName().startsWith("this$") &&
            !Modifier.isTransient(
                modifiers)) {   // speed up: do not count static fields, do not go back up to enclosing object in nested case, do not consider transients
          fields.add(field);
        }
      }
    } catch (Throwable ignored) {
      ExceptionUtils.safelyIgnoreException(ignored);
    }

  }

  /**
   * Return all Fields from a class (including inherited), mapped by
   * String field name to java.lang.reflect.Field.
   *
   * @param c Class whose fields are being fetched.
   * @return Map of all fields on the Class, keyed by String field name to java.lang.reflect.Field.
   */
  public static Map<String, Field> getDeepDeclaredFieldMap(Class c) {
    Map<String, Field> fieldMap = new HashMap<>();
    Collection<Field> fields = getDeepDeclaredFields(c);
    for (Field field : fields) {
      String fieldName = field.getName();
      if (fieldMap.containsKey(
          fieldName)) {   // Can happen when parent and child class both have private field with same name
        fieldMap.put(field.getDeclaringClass().getName() + '.' + fieldName, field);
      } else {
        fieldMap.put(fieldName, field);
      }
    }

    return fieldMap;
  }

  /**
   * Return the name of the class on the object, or "null" if the object is null.
   *
   * @param o Object to get the class name.
   * @return String name of the class or "null"
   */
  public static String getClassName(Object o) {
    return o == null ? "null" : o.getClass().getName();
  }

  public static String getClassNameFromByteCode(byte[] byteCode) throws Exception {
    InputStream is = new ByteArrayInputStream(byteCode);
    DataInputStream dis = new DataInputStream(is);
    dis.readLong(); // skip header and class version
    int cpcnt = (dis.readShort() & 0xffff) - 1;
    int[] classes = new int[cpcnt];
    String[] strings = new String[cpcnt];
    for (int i = 0; i < cpcnt; i++) {
      int t = dis.read();
      if (t == 7) {
        classes[i] = dis.readShort() & 0xffff;
      } else if (t == 1) {
        strings[i] = dis.readUTF();
      } else if (t == 5 || t == 6) {
        dis.readLong();
        i++;
      } else if (t == 8) {
        dis.readShort();
      } else {
        dis.readInt();
      }
    }
    dis.readShort(); // skip access flags
    return strings[classes[(dis.readShort() & 0xffff) - 1] - 1].replace('/', '.');
  }
}
