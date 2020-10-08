package org.dotwebstack.framework.backend.json.scalars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.CoercingParseValueException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonObjectCoercingTest {

  private final JsonObjectCoercing coercing = new JsonObjectCoercing();

  @Test
  void serialize_ShouldReturnMap() {
    Map<Object, Object> map = new HashMap<>();
    map.put("name", "De Brouwerij");

    // Act / Assert
    Map<?, ?> result = coercing.serialize(map);

    assertMapHasResult(result);
  }

  @Test
  void serialize_ThrowsExceptionWhenNotInstanceOfMap() {
    // Act / Assert
    assertThrows(IllegalArgumentException.class, () -> coercing.serialize(new Object()));
  }

  @Test
  void parseMap_ShouldReturnMap() {
    Map<Object, Object> map = new HashMap<>();
    map.put("name", "De Brouwerij");

    // Act / Assert
    Map<?, ?> result = coercing.parseValue(map);

    assertMapHasResult(result);
  }

  @Test
  void parseMap_ThrowsException_whenNotInstanceOfMap() {
    // Act / Assert
    assertThrows(CoercingParseValueException.class, () -> coercing.parseValue(new Object()));
  }

  @Test
  void parseLiteral_ReturnsString_ForModelLiteral() {
    // Arrange
    Map<Object, Object> map = new HashMap<>();
    map.put("name", "De Brouwerij");

    // Act
    Map<?, ?> result = coercing.parseLiteral(map);

    // Assert
    assertMapHasResult(result);
  }

  private void assertMapHasResult(Map<?, ?> result) {
    assertThat(result.containsKey("name"), is(true));
    assertThat(result.containsValue("De Brouwerij"), is(true));
  }
}
