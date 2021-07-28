package org.dotwebstack.framework.service.openapi.response;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.mapping.TypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaSummaryContextValidatorTest {

  private TypeValidator typeValidator;

  @BeforeEach
  public void setup() {
    this.typeValidator = new TypeValidator();
  }

  @Test
  void validate_throwsException_dataTypeMismatchToNumber() {
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("number", "String", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("number", "Boolean", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("number", "ID", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("number", "Char", ""));
  }

  @Test
  void validate_throwsException_dataTypeMismatchToInteger() {
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("integer", "Long", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("integer", "String", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("integer", "Boolean", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("integer", "Float", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("integer", "BigInteger", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("integer", "ID", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("integer", "Char", ""));
  }

  @Test
  void validate_throwsException_dataTypeMismatchToBoolean() {
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("boolean", "Long", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("boolean", "String", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("boolean", "Float", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("boolean", "BigInteger", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("boolean", "ID", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateTypesGraphQlToOpenApi("boolean", "Char", ""));

  }

}
