package org.dotwebstack.framework.service.openapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import com.google.common.collect.Iterables;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OpenApiPropertiesTest {

  private static final String PATH_VALIDATION_MSG =
      "\"^(?:(\\/(?:(?:(?:[A-Za-z0-9-._~])|(?:%(?:[0-9ABCDEF]){2})|(?:[!$&'()*+,;=])|[:@])*))*)$\"";

  private Validator validator;

  private OpenApiProperties properties;

  @BeforeEach
  public void setUp() {
    validator = Validation.buildDefaultValidatorFactory()
        .getValidator();
    properties = new OpenApiProperties();
  }

  @Test
  void defaultApiDocPublicationPath_isValid() {
    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, empty());
  }

  @Test
  void setApiDocPublicationPath_withValidValue_isValid() {
    properties.setApiDocPublicationPath("/openapi.yaml");

    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, empty());
  }

  @Test
  void setApiDocPublicationPath_withComplexValidValue_isValid() {
    properties.setApiDocPublicationPath("/some/path/open%20api%2F~$@_adsfa30=-+");

    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, empty());
  }

  @ParameterizedTest
  @CsvSource({"openapi.yaml", "/open%2Gapi", "/open^api"})
  void setApiDocPublicationPath_withInvalidValue_isInvalid(String apiDocPublicationPath) {
    properties.setApiDocPublicationPath(apiDocPublicationPath);

    var validationResult = validator.validate(properties);

    assertThat(validationResult, hasSize(1));

    assertThat(Iterables.getOnlyElement(validationResult)
        .getMessage(), endsWith(PATH_VALIDATION_MSG));
  }

  @Test
  void setApiDocPublicationPath_withIllegalCharacter_isInvalid() {
    properties.setApiDocPublicationPath("/open^api");

    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, hasSize(1));

    assertThat(Iterables.getOnlyElement(validationResult)
        .getMessage(), endsWith(PATH_VALIDATION_MSG));
  }

}
