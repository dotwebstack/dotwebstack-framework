package org.dotwebstack.framework.core.helpers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

class ObjectHelperTest {

  @Test
  void cast_returnsCastedObject_forGivenClassAndObject() {
    assertNotNull(ObjectHelper.cast(String.class, "foo"));
  }

  @Test
  void cast_throwsIllegalArgumentException_forWrongClassAndObject() {
    assertThrows(IllegalArgumentException.class,() ->
            ObjectHelper.cast(String.class, 1L));
  }

  @Test
  void cast_throwsIllegalArgumentException_forWrongClassAndNullObject() {
    assertThrows(IllegalArgumentException.class,() ->
            ObjectHelper.cast(String.class, null));
  }

  @Test
  void castToList_returnsCastedObject_forGivenObject() {
    assertNotNull(ObjectHelper.castToList(new ArrayList<>()));
  }

  @Test
  void castToMap_returnsCastedObject_forGivenObject() {
    assertNotNull(ObjectHelper.castToMap(new HashMap<>()));
  }
}