package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.ArrayList;
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

  public static Object[] castToArray(Object value, String type) {
    if(type.equals("String")){
      return castToList(value).toArray(String[]::new);
    }else if(type.equals("Int")){
      return castToList(value).toArray(Integer[]::new);
    }
    return castToList(value).toArray(String[]::new);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> castToMap(Object value) {
    return cast(Map.class, value);
  }
}
