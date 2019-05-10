package org.dotwebstack.framework.core.helpers;

import static org.springframework.util.Assert.isInstanceOf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ObjectHelperTest {

  @Test
  void cast_returnsCastedObject_forGivenClassAndObject() {
    isInstanceOf(String.class,ObjectHelper.cast(String.class, "foo"));
  }

  @Test
  void cast_throwsIllegalArgumentException_forWrongClassAndObject() {
    Assertions.assertThrows(IllegalArgumentException.class,() ->
            ObjectHelper.cast(String.class, 1L));
  }

  @Test
  void cast_throwsIllegalArgumentException_forWrongClassAndNullObject() {
    Assertions.assertThrows(IllegalArgumentException.class,() ->
            ObjectHelper.cast(String.class, null));
  }

  @Test
  void castToList_returnsCastedObject_forGivenObject() {
    isInstanceOf(List.class,ObjectHelper.castToList(new ArrayList<>()));
  }

  @Test
  void castToMap_returnsCastedObject_forGivenObject() {
    isInstanceOf(Map.class,ObjectHelper.castToMap(new HashMap<>()));
  }
}