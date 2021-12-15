package org.dotwebstack.framework.core.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.channel.group.ChannelMatchers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hamcrest.core.Is;
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

    var expectedArray = new Integer[]{33, 44};
    assertThat(array, is(expectedArray));
  }

  @Test
  void castToArray_returnsArray_forListWithStrings() {
    var array = ObjectHelper.castToArray(List.of("foo", "bar"), "String");

    var expectedArray = new String[]{"foo", "bar"};
    assertThat(array, is(expectedArray));
  }

  @Test
  void castToArray_returnsArray_forListWithFloats() {
    var array = ObjectHelper.castToArray(List.of(33.3f, 44.4f), "Float");

    var expectedArray = new Float[]{33.3f, 44.4f};
    assertThat(array, is(expectedArray));
  }

  @Test
  void castToArray_throwsIllegalArgumentException_forUnknownType() {
    assertThrows(IllegalArgumentException.class, () -> ObjectHelper.castToArray(List.of(33.3f, 44.4f), "Unknown"));
  }

  @Test
  void castToMap_returnsCastedObject_forGivenObject() {
    assertNotNull(ObjectHelper.castToMap(new HashMap<>()));
  }
}
