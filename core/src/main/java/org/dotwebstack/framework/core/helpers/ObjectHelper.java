package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class ObjectHelper {

  private ObjectHelper() {}

  public static <T> T cast(Class<T> clazz, Object value) {
    if (value == null) {
      throw new IllegalArgumentException("Object null!");
    }
    if (!(clazz.isAssignableFrom(value.getClass()))) {
      throw illegalArgumentException("Object class '{}' not instance of {}!", value.getClass()
          .getSimpleName(), clazz.getSimpleName());
    }

    return clazz.cast(value);
  }

  @SuppressWarnings("unchecked")
  public static List<Object> castToList(Object value) {
    return cast(List.class, value);
  }

  public static <T> List<T> castToList(Object value, Class<T> clazz) {
    return cast(List.class, value);
  }


  public static <T> T[] castToArray(Object value, Class<T> type, boolean toLower) {
    var list = castToList(value);
    T[] result = (T[]) Array.newInstance(type, list.size());
    for (int i = 0; i < list.size(); i++) {
      var item = (T) list.get(i);
      if (toLower && String.class.equals(type)) {
        item = (T) ((String) item).toLowerCase();
      }
      result[i] = item;
    }
    return result;
  }

  public static Object[] castToArray(Object value, String type) {
    if (type.equals("String")) {
      return castToList(value).toArray(String[]::new);
    } else if (type.equals("Int")) {
      return castToList(value).toArray(Integer[]::new);
    } else if (type.equals("Float")) {
      return castToList(value).toArray(Float[]::new);
    }
    throw illegalArgumentException("Object with type '{}' can not be casted to an array.", type);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> castToMap(Object value) {
    return cast(Map.class, value);
  }
}
