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
import org.springframework.http.HttpMethod;

public class ResponseSchemaContextValidatorTest {

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
    ResponseSchemaContext getResponseSchemaContext = getResponseContext("/query1", HttpMethod.GET);

    // Act / Assert
    this.validator.validate(getResponseSchemaContext.getResponses()
        .get(0)
        .getResponseObject(), getResponseSchemaContext.getGraphQlField());
  }

  @Test
  public void validate_succeeds_query1Post() {
    // Arrange
    ResponseSchemaContext getResponseSchemaContext = getResponseContext("/query1", HttpMethod.POST);

    // Act / Assert
    this.validator.validate(getResponseSchemaContext.getResponses()
        .get(0)
        .getResponseObject(), getResponseSchemaContext.getGraphQlField());
  }

  @Test
  public void validate_succeeds_query2Get() {
    // Arrange
    ResponseSchemaContext getResponseSchemaContext = getResponseContext("/query2", HttpMethod.GET);

    // Act / Assert
    this.validator.validate(getResponseSchemaContext.getResponses()
        .get(0)
        .getResponseObject(), getResponseSchemaContext.getGraphQlField());
  }

  @Test
  public void validate_throwsException_graphQlFieldNotFound() throws IOException {
    // Arrange
    this.registry = TestResources.typeDefinitionRegistry("o2_prop1", "other_property");
    ResponseSchemaContext getResponseSchemaContext = getResponseContext("/query1", HttpMethod.GET);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.validator.validate(getResponseSchemaContext.getResponses()
            .get(0)
            .getResponseObject(), getResponseSchemaContext.getGraphQlField()));
  }

  @Test
  public void validate_throwsException_typeMismatch() throws IOException {
    // Arrange
    this.registry = TestResources.typeDefinitionRegistry("o2_prop1", "other_property");
    ResponseSchemaContext getResponseSchemaContext = getResponseContext("/query1", HttpMethod.GET);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.validator.validate(getResponseSchemaContext.getResponses()
            .get(0)
            .getResponseObject(), getResponseSchemaContext.getGraphQlField()));
  }

  @Test
  public void validate_throwsException_dataTypeMismatchToNumber() {
    // Act / Assert
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
  public void validate_throwsException_dataTypeMismatchToInteger() {
    // Act / Assert
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
  public void validate_throwsException_dataTypeMismatchToBoolean() {
    // Act / Assert
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

  @Test
  public void validate_throwsException_dataTypeMismatchStringToInteger() throws IOException {
    // Arrange
    this.registry = TestResources.typeDefinitionRegistry("o1_prop2: Float!", "o1_prop2: Boolean!");
    ResponseSchemaContext getResponseSchemaContext = getResponseContext("/query1", HttpMethod.GET);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.validator.validate(getResponseSchemaContext.getResponses()
            .get(0)
            .getResponseObject(), getResponseSchemaContext.getGraphQlField()));
  }

  private ResponseSchemaContext getResponseContext(String path, HttpMethod httpMethod) {

    PathItem pathItem = this.openApi.getPaths()
        .get(path);
    List<ResponseTemplate> responses = ResponseTemplateBuilderTest.getResponseTemplates(this.openApi, path, httpMethod);
    GraphQlField field = TestResources.queryFieldHelper(this.registry)
        .resolveGraphQlField(pathItem.getGet());

    return new ResponseSchemaContext(field, responses, Collections.emptyList(), null);
  }
}
