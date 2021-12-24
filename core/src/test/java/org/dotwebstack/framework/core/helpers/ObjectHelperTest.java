package org.dotwebstack.framework.core.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

class ObjectHelperTest {

  @Test
  void cast_returnsCastedObject_forGivenClassAndObject() {
    assertNotNull(ObjectHelper.cast(String.class, "foo"));
  }

  @Test
  void cast_throwsIllegalArgumentException_forWrongClassAndObject() {
    assertThrows(IllegalArgumentException.class, () -> ObjectHelper.cast(String.class, 1L));
  }

  @Test
  void cast_throwsIllegalArgumentException_forWrongClassAndNullObject() {
    assertThrows(IllegalArgumentException.class, () -> ObjectHelper.cast(String.class, null));
  }

  @Test
  void castToList_returnsCastedObject_forGivenObject() {
    assertNotNull(ObjectHelper.castToList(new ArrayList<>()));
  }

  @Test
  void castToArray_returnsArray_forListWithIntegers() {
    var array = ObjectHelper.castToArray(List.of(33, 44), "Int");

    var expectedArray = new Integer[] {33, 44};
    assertThat(array, is(expectedArray));
  }

  @Test
  void castToArray_returnsArray_forListWithStrings() {
    var array = ObjectHelper.castToArray(List.of("foo", "bar"), "String");

    var expectedArray = new String[] {"foo", "bar"};
    assertThat(array, is(expectedArray));
  }

  @Test
  void castToArray_returnsArrayWithLowercaseValues_forListWithStrings() {
    var array = ObjectHelper.castToArray(List.of("Foo", "Bar"), String.class, true);

    var expectedArray = new String[] {"foo", "bar"};
    assertThat(array, is(expectedArray));
  }

  @Test
  void castToArray_returnsArrayWithUnchangedValues_forListWithStrings() {
    var array = ObjectHelper.castToArray(List.of("Foo", "Bar"), String.class, false);

    var expectedArray = new String[] {"Foo", "Bar"};
    assertThat(array, is(expectedArray));
  }

  @Test
  void castToArray_returnsArray_forListWithFloats() {
    var array = ObjectHelper.castToArray(List.of(33.3f, 44.4f), "Float");

    var expectedArray = new Float[] {33.3f, 44.4f};
    assertThat(array, is(expectedArray));
  }

  @Test
  void castToArray_returnsArrayWithUnchangedValues_forListWithFloatsAndToLower() {
    var array = ObjectHelper.castToArray(List.of(33.3f, 44.4f), Float.class, true);

    var expectedArray = new Float[] {33.3f, 44.4f};
    assertThat(array, is(expectedArray));
  }

  @Test
  void castToArray_throwsIllegalArgumentException_forUnknownType() {
    var listToCast = List.of(33.3f, 44.4f);
    assertThrows(IllegalArgumentException.class, () -> ObjectHelper.castToArray(listToCast, "Unknown"));
  }

  @Test
  void castToMap_returnsCastedObject_forGivenObject() {
    assertNotNull(ObjectHelper.castToMap(new HashMap<>()));
  }
}
