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

public class OpenApiPropertiesTest {

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

  @Test
  void setApiDocPublicationPath_withInvalidValue_isInvalid() {
    properties.setApiDocPublicationPath("openapi.yaml");

    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, hasSize(1));

    assertThat(Iterables.getOnlyElement(validationResult)
        .getMessage(), endsWith(PATH_VALIDATION_MSG));
  }

  @Test
  void setApiDocPublicationPath_withWrongPercentEncoding_isInvalid() {
    properties.setApiDocPublicationPath("/open%2Gapi");

    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

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
