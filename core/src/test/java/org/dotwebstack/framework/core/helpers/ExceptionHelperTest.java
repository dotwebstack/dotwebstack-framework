package org.dotwebstack.framework.core.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExceptionHelperTest {

  @Test
  void joinArguments_returnsJoinedArray_forGivenArguments() {
    Exception cause = new IllegalArgumentException("Oops!");
    Object[] result = ExceptionHelper.joinArguments(cause,new String[]{"foo","bar"});
    assertEquals(3,result.length);
    assertEquals(cause,result[2]);
  }
}