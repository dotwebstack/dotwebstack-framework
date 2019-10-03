package org.dotwebstack.framework.service.openapi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;

class CoreRequestHandlerTest {

  private CoreRequestHandler coreRequestHandler = new CoreRequestHandler();

  @Test
  public void parameterValidation_throwsError_withUnexistingParam() {
    // arrange
    Set<String> schemaParams = Set.of("extends");
    Set<String> givenParams = Set.of("cat", "dog");

    // act & assert
    assertThrows(InvalidConfigurationException.class,
        () -> coreRequestHandler.validateParameterExistence("path", schemaParams, givenParams));
  }

  @Test
  public void parameterValidation_throwsError_withPArtiallyUnexistingParams() {
    // arrange
    Set<String> schemaParams = Set.of("cat");
    Set<String> givenParams = Set.of("cat", "dog");

    // act & assert
    assertThrows(InvalidConfigurationException.class,
        () -> coreRequestHandler.validateParameterExistence("path", schemaParams, givenParams));
  }

  @Test
  public void parameterValidation_doesNotThrowError_withOnlyExistingParam() {
    // arrange
    Set<String> schemaParams = Set.of("cat", "dog");
    Set<String> givenParams = Set.of("cat");

    // act & assert
    assertDoesNotThrow(() -> coreRequestHandler.validateParameterExistence("path", schemaParams, givenParams));
  }

}
