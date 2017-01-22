package rocks.appconcept.javatools.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author yanimetaxas
 */
public class JSON {

  private JSON() {
  }

  /**
   * Make JSON from an object
   */
  public static String stringify(Object o) throws JSONException {
    try {
      JSONStream stream = new JSONStream();
      stringify(stream, null, o);
      return stream.toJSON();
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new JSONException(e.getMessage(), e);
    }
  }

  /**
   * Make an object from JSON
   */
  public static <T> T parse(Class<? extends T> type, String json) throws JSONException {
    try {
      JSONObject parse = JSONObject.parse(json);
      return type.cast(parse(type, parse));
    } catch (Exception e) {
      throw new JSON.JSONException(e.getMessage(), e);
    }
  }

  private static Object parse(Type type, JSONObject json)
      throws IllegalAccessException, InstantiationException {
    if (json == JSONObject.NULL || json == JSONObject.UNDEFINED) {
      return null;
    } else if (type instanceof ParameterizedType) {
      Type rawType = ((ParameterizedType) type).getRawType();
      if (rawType instanceof Class && List.class.isAssignableFrom((Class<?>) rawType)) {
        Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];

        List<Object> result = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
          result.add(parse(elementType, json.get(i)));
        }
        return result;
      }

      if (rawType instanceof Class && Map.class.isAssignableFrom((Class<?>) rawType)) {
        if (String.class != ((ParameterizedType) type).getActualTypeArguments()[0]) {
          throw new IllegalArgumentException("Only maps with String keys can be parsed");
        }
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];

        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, JSONObject> entry : json.entrySet()) {
          result.put(entry.getKey(), parse(valueType, entry.getValue()));
        }
        return result;
      }
    } else if (type instanceof Class) {

      Class classType = (Class) type;

      if (classType == Boolean.TYPE || classType == Boolean.class) {
        return json.asBoolean();
      } else if (classType == String.class) {
        return json.asString();
      } else if (classType.isEnum()) {
        return Enum.valueOf(classType, json.asString());
      } else if (classType == Float.TYPE || classType == Float.class) {
        return json.asFloat();
      } else if (classType == Integer.TYPE || classType == Integer.class) {
        return json.asInteger();
      } else if (classType == Long.TYPE || classType == Long.class) {
        return json.asLong();
      } else if (classType == Double.TYPE || classType == Double.class) {
        return json.asDouble();
      } else if (classType == Short.TYPE || classType == Short.class) {
        return json.asInteger();
      } else if (classType == Byte.TYPE || classType == Byte.class) {
        return json.asInteger();
      } else if (classType == Character.TYPE || classType == Character.class) {
        return json.asInteger();
      } else if (classType.isArray()) {
        Object ar = Array.newInstance(classType.getComponentType(), json.length());
        for (int i = 0; i < json.length(); i++) {
          Array.set(ar, i, parse(classType.getComponentType(), json.get(i)));
        }
        return classType.cast(ar);
      } else {
        Object bean = classType.newInstance();
        for (Field field : classType.getDeclaredFields()) {
          if ((field.getModifiers() & Modifier.STATIC) == 0) {
            field.setAccessible(true);
            Object fieldValue = parse(field.getGenericType(), json.get(field.getName()));
            if (fieldValue != null || !field.getType()
                .isPrimitive()) { // don't set null to primitive fields
              field.set(bean, fieldValue);
            }
          }
        }
        return bean;
      }
    }

    throw new RuntimeException("Unhandled type: " + type.getClass().getName());
  }

  private static void stringify(JSONStream stream, String property, Object bean)
      throws InvocationTargetException, IllegalAccessException {
    if (bean == null) {
      stream.value(property, null);
    } else if (bean instanceof Boolean) {
      stream.value(property, ((Boolean) bean));
    } else if (bean instanceof String) {
      stream.value(property, ((String) bean));
    } else if (bean instanceof Enum) {
      stream.value(property, ((Enum) bean).name());
    } else if (bean instanceof Float) {
      stream.value(property, ((Float) bean));
    } else if (bean instanceof Integer) {
      stream.value(property, ((Integer) bean));
    } else if (bean instanceof Short) {
      stream.value(property, ((Short) bean));
    } else if (bean instanceof Byte) {
      stream.value(property, ((Byte) bean));
    } else if (bean instanceof Character) {
      stream.value(property, ((Character) bean));
    } else if (bean instanceof Long) {
      stream.value(property, ((Long) bean));
    } else if (bean instanceof Double) {
      stream.value(property, ((Double) bean));
    } else if (bean instanceof List) {
      stream.list(property);
      for (Object element : ((List) bean)) {
        stringify(stream, null, element);
      }
      stream.endlist();
    } else if (bean instanceof Map) {
      stream.object(property);
      for (Map.Entry<?, ?> element : ((Map<?, ?>) bean).entrySet()) {
        stringify(stream, String.valueOf(element.getKey()), element.getValue());
      }
      stream.endobject();
    } else {
      Class<?> valueClass = bean.getClass();

      if (valueClass.isArray()) {
        stream.list(property);
        int length = Array.getLength(bean);
        for (int i = 0; i < length; i++) {
          stringify(stream, null, Array.get(bean, i));
        }
        stream.endlist();
      } else {
        Optional<Method> toJSON = Stream.of(valueClass.getMethods()).filter(
            x -> x.getName().equals("toJSON") && x.getReturnType() == String.class
                && x.getParameterCount() == 0).findAny();
        if (toJSON.isPresent()) {
          stream.verbatim(property, (String) toJSON.get().invoke(bean));
        } else {
          stream.object(property);
          for (Field field : valueClass.getDeclaredFields()) {
            if ((field.getModifiers() & Modifier.STATIC) == 0) {
              field.setAccessible(true);
              stringify(stream, field.getName(), field.get(bean));
            }
          }
          stream.endobject();
        }
      }
    }
  }

  public static class JSONException extends RuntimeException {

    public JSONException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
