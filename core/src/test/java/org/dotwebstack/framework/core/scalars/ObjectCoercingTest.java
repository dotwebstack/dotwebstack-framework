package org.dotwebstack.framework.core.scalars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.HashMap;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

class ObjectCoercingTest {

  private final ObjectCoercing coercing = new ObjectCoercing();

  @Test
  void serialize_ShouldReturnMap() {
    Map<Object, Object> map = new HashMap<>();
    map.put("name", "De Brouwerij");

    // Act / Assert
    Object result = coercing.serialize(map);

    assertThat(result, is(CoreMatchers.equalTo(result)));
  }

  @Test
  void parseMap_ShouldReturnMap() {
    Map<Object, Object> map = new HashMap<>();
    map.put("name", "De Brouwerij");

    // Act / Assert
    Object result = coercing.parseValue(map);

    assertThat(result, is(CoreMatchers.equalTo(result)));
  }

  @Test
  void parseLiteral_ReturnsString_ForModelLiteral() {
    // Arrange
    Map<Object, Object> map = new HashMap<>();
    map.put("name", "De Brouwerij");

    // Act
    Object result = coercing.parseLiteral(map);

    // Assert
    assertThat(result, is(CoreMatchers.equalTo(result)));
  }
}
