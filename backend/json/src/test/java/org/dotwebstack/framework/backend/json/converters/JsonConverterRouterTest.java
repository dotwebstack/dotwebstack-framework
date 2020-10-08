package org.dotwebstack.framework.backend.json.converters;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class JsonConverterRouterTest {

  private final JsonConverterRouter jsonConverterRouter = new JsonConverterRouter();

  @Test
  void convertFromValueShouldThrowException() {
    // Act Assert
    assertThrows(UnsupportedOperationException.class, () -> jsonConverterRouter.convertFromValue(new Object()));
  }

  @Test
  void convertToValueShouldThrowException() {
    // Act Assert
    assertThrows(UnsupportedOperationException.class, () -> jsonConverterRouter.convertToValue(new Object(), "String"));
  }

}
