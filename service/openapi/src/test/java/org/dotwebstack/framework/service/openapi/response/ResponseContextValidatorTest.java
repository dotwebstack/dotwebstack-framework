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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResponseContextValidatorTest {

  private OpenAPI openApi;

  private TypeDefinitionRegistry registry;

  private ResponseContextValidator validator;

  @BeforeEach
  public void setup() {
    this.openApi = TestResources.openApi();
    this.registry = TestResources.typeDefinitionRegistry();
    this.validator = new ResponseContextValidator();
  }

  @Test
  public void validate_succeeds_query1Get() {
    // Arrange
    ResponseContextValidator validator = new ResponseContextValidator();
    ResponseContext getResponseContext = getResponseContext("/query1", "get");

    // Act / Assert
    validator.validate(getResponseContext, "/query1");
  }

  @Test
  public void validate_succeeds_query1Post() {
    // Arrange
    ResponseContext getResponseContext = getResponseContext("/query1", "post");

    // Act / Assert
    this.validator.validate(getResponseContext, "/query1");
  }

  @Test
  public void validate_succeeds_query2Get() {
    // Arrange
    ResponseContext getResponseContext = getResponseContext("/query2", "get");

    // Act / Assert
    this.validator.validate(getResponseContext, "/query2");
  }

  @Test
  public void validate_throwsException_missing200() {
    // Arrange
    ResponseContext getResponseContext = getResponseContext("/query2", "get");
    getResponseContext.getResponses()
        .get(0)
        .setResponseCode(300);

    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () -> this.validator.validate(getResponseContext, "/query2"));
  }

  @Test
  public void validate_throwsException_graphQlFieldNotFound() throws IOException {
    // Arrange
    this.registry = TestResources.typeDefinitionRegistry("o2_prop1", "other_property");
    ResponseContext getResponseContext = getResponseContext("/query1", "get");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validate(getResponseContext, "/query2"));
  }

  @Test
  public void validate_throwsException_typeMismatch() throws IOException {
    // Arrange
    this.registry = TestResources.typeDefinitionRegistry("o2_prop1", "other_property");
    ResponseContext getResponseContext = getResponseContext("/query1", "get");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validate(getResponseContext, "/query1"));
  }

  @Test
  public void validate_throwsException_dataTypeMismatchToNumber() {
    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("number", "String", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("number", "Boolean", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("number", "ID", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("number", "Char", ""));
  }

  @Test
  public void validate_throwsException_dataTypeMismatchToInteger() {
    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("integer", "Long", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("integer", "String", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("integer", "Boolean", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("integer", "Float", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("integer", "BigInteger", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("integer", "ID", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("integer", "Char", ""));
  }

  @Test
  public void validate_throwsException_dataTypeMismatchToBoolean() {
    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("boolean", "Long", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("boolean", "String", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("boolean", "Float", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("boolean", "BigInteger", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("boolean", "ID", ""));
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validateTypes("boolean", "Char", ""));

  }

  @Test
  public void validate_throwsException_dataTypeMismatchStringToInteger() throws IOException {
    // Arrange
    this.registry = TestResources.typeDefinitionRegistry("o1_prop2: Float!", "o1_prop2: Boolean!");
    ResponseContext getResponseContext = getResponseContext("/query1", "get");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> this.validator.validate(getResponseContext, "/query1"));
  }

  private ResponseContext getResponseContext(String path, String methodName) {

    PathItem pathItem = this.openApi.getPaths()
        .get(path);
    List<ResponseTemplate> responses = ResponseTemplateBuilderTest.getResponseTemplates(this.openApi, path, methodName);
    GraphQlField field = TestResources.queryFieldHelper(this.registry)
        .resolveGraphQlField(pathItem);

    return new ResponseContext(field, responses, Collections.emptyList());
  }
}
