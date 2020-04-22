package org.dotwebstack.framework.service.openapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;

import com.google.common.collect.Iterables;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OpenApiPropertiesTest {

  private static final String PATH_VALIDATION_MSG =
      "must match \"^(?:(\\/(?:(?:(?:[A-Za-z0-9-._~])|(?:%(?:[0-9ABCDEF]){2})|(?:[!$&'()*+,;=])|[:@])*))*)$\"";

  private Validator validator;

  private OpenApiProperties properties;

  @BeforeEach
  public void setUp() {
    validator = Validation.buildDefaultValidatorFactory()
        .getValidator();
    properties = new OpenApiProperties();
  }

  @Test
  public void defaultApiDocPublicationPath_isValid() {
    // Assert
    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, empty());
  }

  @Test
  public void setApiDocPublicationPath_withValidValue_isValid() {
    // Act
    properties.setApiDocPublicationPath("/openapi.yaml");

    // Assert
    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, empty());
  }

  @Test
  public void setApiDocPublicationPath_withComplexValidValue_isValid() {
    // Act
    properties.setApiDocPublicationPath("/some/path/open%20api%2F~$@_adsfa30=-+");

    // Assert
    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, empty());
  }

  @Test
  public void setApiDocPublicationPath_withInvalidValue_isInvalid() {
    // Act
    properties.setApiDocPublicationPath("openapi.yaml");

    // Assert
    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, hasSize(1));

    assertThat(Iterables.getOnlyElement(validationResult)
        .getMessage(), is(PATH_VALIDATION_MSG));
  }

  @Test
  public void setApiDocPublicationPath_withWrongPercentEncoding_isInvalid() {
    // Act
    properties.setApiDocPublicationPath("/open%2Gapi");

    // Assert
    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, hasSize(1));

    assertThat(Iterables.getOnlyElement(validationResult)
        .getMessage(), is(PATH_VALIDATION_MSG));
  }

  @Test
  public void setApiDocPublicationPath_withIllegalCharacter_isInvalid() {
    // Act
    properties.setApiDocPublicationPath("/open^api");

    // Assert
    Set<ConstraintViolation<OpenApiProperties>> validationResult = validator.validate(properties);

    assertThat(validationResult, hasSize(1));

    assertThat(Iterables.getOnlyElement(validationResult)
        .getMessage(), is(PATH_VALIDATION_MSG));
  }

}
