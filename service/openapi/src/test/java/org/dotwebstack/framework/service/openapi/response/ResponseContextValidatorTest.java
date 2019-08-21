package org.dotwebstack.framework.service.openapi.response;

import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.mapping.TypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResponseContextValidatorTest {

  private OpenAPI openApi;

  private TypeDefinitionRegistry registry;

  private ResponseContextValidator validator;

  private TypeValidator typeValidator;

  @BeforeEach
  public void setup() {
    this.openApi = TestResources.openApi();
    this.registry = TestResources.typeDefinitionRegistry();
    this.validator = new ResponseContextValidator();
    this.typeValidator = new TypeValidator();
  }

  @Test
  public void validate_succeeds_query1Get() {
    // Arrange
    ResponseContext getResponseContext = getResponseContext("/query1", "get");

    // Act / Assert
    this.validator.validate(getResponseContext.getResponses()
        .get(0)
        .getResponseObject(), getResponseContext.getGraphQlField());
  }

  @Test
  public void validate_succeeds_query1Post() {
    // Arrange
    ResponseContext getResponseContext = getResponseContext("/query1", "post");

    // Act / Assert
    this.validator.validate(getResponseContext.getResponses()
        .get(0)
        .getResponseObject(), getResponseContext.getGraphQlField());
  }

  @Test
  public void validate_succeeds_query2Get() {
    // Arrange
    ResponseContext getResponseContext = getResponseContext("/query2", "get");

    // Act / Assert
    this.validator.validate(getResponseContext.getResponses()
        .get(0)
        .getResponseObject(), getResponseContext.getGraphQlField());
  }

  @Test
  public void validate_throwsException_graphQlFieldNotFound() throws IOException {
    // Arrange
    this.registry = TestResources.typeDefinitionRegistry("o2_prop1", "other_property");
    ResponseContext getResponseContext = getResponseContext("/query1", "get");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validate(getResponseContext.getResponses()
        .get(0)
        .getResponseObject(), getResponseContext.getGraphQlField()));
  }

  @Test
  public void validate_throwsException_typeMismatch() throws IOException {
    // Arrange
    this.registry = TestResources.typeDefinitionRegistry("o2_prop1", "other_property");
    ResponseContext getResponseContext = getResponseContext("/query1", "get");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validate(getResponseContext.getResponses()
        .get(0)
        .getResponseObject(), getResponseContext.getGraphQlField()));
  }

  @Test
  public void validate_throwsException_dataTypeMismatchToNumber() {
    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("number", "String", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("number", "Boolean", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("number", "ID", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("number", "Char", ""));
  }

  @Test
  public void validate_throwsException_dataTypeMismatchToInteger() {
    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("integer", "Long", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("integer", "String", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("integer", "Boolean", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("integer", "Float", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("integer", "BigInteger", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("integer", "ID", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("integer", "Char", ""));
  }

  @Test
  public void validate_throwsException_dataTypeMismatchToBoolean() {
    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("boolean", "Long", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("boolean", "String", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("boolean", "Float", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("boolean", "BigInteger", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("boolean", "ID", ""));
    assertThrows(InvalidConfigurationException.class,
        () -> this.typeValidator.validateOpenApiToGraphQlTypes("boolean", "Char", ""));

  }

  @Test
  public void validate_throwsException_dataTypeMismatchStringToInteger() throws IOException {
    // Arrange
    this.registry = TestResources.typeDefinitionRegistry("o1_prop2: Float!", "o1_prop2: Boolean!");
    ResponseContext getResponseContext = getResponseContext("/query1", "get");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validate(getResponseContext.getResponses()
        .get(0)
        .getResponseObject(), getResponseContext.getGraphQlField()));
  }

  private ResponseContext getResponseContext(String path, String methodName) {

    PathItem pathItem = this.openApi.getPaths()
        .get(path);
    List<ResponseTemplate> responses = ResponseTemplateBuilderTest.getResponseTemplates(this.openApi, path, methodName);
    GraphQlField field = TestResources.queryFieldHelper(this.registry)
        .resolveGraphQlField(pathItem.getGet());

    return new ResponseContext(field, responses, Collections.emptyList(), null);
  }
}
