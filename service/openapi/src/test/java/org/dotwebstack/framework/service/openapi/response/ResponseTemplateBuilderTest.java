package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_VALUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class ResponseTemplateBuilderTest {

  private OpenAPI openApi;

  @BeforeEach
  void setup() {
    this.openApi = TestResources.openApi();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/query1", "/query2"})
  void build_returnsTemplates_ForValidSpec() {
    // Arrange / Act
    List<ResponseTemplate> responseTemplates = getResponseTemplates(this.openApi, "/query1", HttpMethod.GET);

    // Assert
    assertEquals(1, responseTemplates.size());
    ResponseTemplate okResponse = responseTemplates.stream()
        .filter(rt -> rt.isApplicable(200, 299))
        .findFirst()
        .orElseThrow(() -> new InvalidConfigurationException(""));

    assertEquals(MediaType.valueOf("application/hal+json"), okResponse.getMediaType());
    assertEquals(200, okResponse.getResponseCode());
    assertEquals("object", okResponse.getResponseObject()
        .getSummary()
        .getType());
  }

  @Test
  void build_throwsException_MissingSchema() {
    // Arrange
    openApi.getComponents()
        .getSchemas()
        .remove("Object1");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query1", HttpMethod.GET));
  }

  @Test
  void build_throwsException_MissingSchema2() {
    // Arrange
    openApi.getComponents()
        .getSchemas()
        .remove("Object2");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query1", HttpMethod.GET));
  }

  @Test
  void build_resolvesXdwsTemplate_forValidSchema() {
    // Act
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query1", HttpMethod.GET);

    // Assert
    assertEquals(1, templates.size());
    ResponseTemplate responseTemplate = templates.get(0);
    assertEquals(1, responseTemplate.getResponseObject()
        .getSummary()
        .getChildren()
        .stream()
        .filter(wrapper -> Objects.nonNull(wrapper.getSummary()
            .getDwsExpr()))
        .filter(wrapper -> "template_content".equals(wrapper.getSummary()
            .getDwsExpr()
            .get(X_DWS_EXPR_VALUE)))
        .count());
  }

  @Test
  void build_resolvesSchema_forRecursiveObjectSchema() {
    // Act
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query7", HttpMethod.GET);

    // Assert
    assertEquals(1, templates.size());
  }

  @Test
  void build_resolvesSchema_forRecursiveArraySchema() {
    // Act
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query8", HttpMethod.GET);

    // Assert
    assertEquals(1, templates.size());
  }

  @Test
  void build_resolvesAllOfTemplate_forValidSchema() {
    // Act
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query5", HttpMethod.GET);

    // Assert
    assertEquals(2, templates.size());
    ResponseTemplate responseTemplate = templates.get(0);
    assertEquals(2, responseTemplate.getResponseObject()
        .getSummary()
        .getComposedOf()
        .size());
  }

  @Test
  void build_throwsException_ObjectXdwsTemplateType() {
    // Arrange
    Schema<?> property1 = (Schema) openApi.getComponents()
        .getSchemas()
        .get("Object1")
        .getProperties()
        .get("o1_prop1");
    property1.setType("object");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query1", HttpMethod.GET));
  }

  @Test
  void build_throwsException_ArrayXdwsTemplateType() {
    // Arrange
    Schema<?> property1 = (Schema) openApi.getComponents()
        .getSchemas()
        .get("Object1")
        .getProperties()
        .get("o1_prop1");
    property1.setType("array");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query1", HttpMethod.GET));
  }

  @Test
  void build_throwsException_IntegerXdwsTemplateType() {
    // Arrange
    Schema<?> property1 = (Schema) openApi.getComponents()
        .getSchemas()
        .get("Object1")
        .getProperties()
        .get("o1_prop1");
    property1.setType("integer");

    // Act / Assert
    assertDoesNotThrow(() -> getResponseTemplates(this.openApi, "/query1", HttpMethod.GET));
  }

  @Test
  void build_throwsException_MissingXDwsExprInHeaderSchema() {
    // Arrange
    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getHeaders()
        .get("X-Response-Header")
        .getSchema()
        .getExtensions()
        .remove("x-dws-expr");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query6", HttpMethod.GET));
  }

  @Test
  @SuppressWarnings("unchecked")
  void build_throwsException_MissingExtensionsInHeaderSchema() {
    // Arrange
    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getHeaders()
        .get("X-Response-Header")
        .getSchema()
        .setExtensions(null);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query6", HttpMethod.GET));
  }

  @Test
  void build_throwsException_without_configuredXdwsStringType() {
    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, ImmutableList.of("unknown type"), "/query6", HttpMethod.GET));
  }

  @Test
  void build_succeeds_with_configuredXdwsStringType() {
    // Act / Assert
    List<ResponseTemplate> responseTemplates =
        getResponseTemplates(this.openApi, ImmutableList.of("customType"), "/query6", HttpMethod.GET);

    assertEquals(1, responseTemplates.size());
    assertEquals("string", responseTemplates.get(0)
        .getResponseObject()
        .getSummary()
        .getType());
    assertTrue(responseTemplates.get(0)
        .getResponseObject()
        .getSummary()
        .getSchema() instanceof StringSchema);
  }

  @Test
  void getXdwsType_returns_expectedValue() {
    // Arrange
    Schema<?> schema = this.openApi.getComponents()
        .getSchemas()
        .get("Object5");

    // Act
    Optional<String> xdwsType = ResponseTemplateBuilder.getXdwsType(schema);

    // Assert
    assertTrue(xdwsType.isPresent());
    assertEquals("customType", xdwsType.get());
  }

  @Test
  void getXdwsType_returns_empty() {
    // Arrange
    Schema<?> schema = this.openApi.getComponents()
        .getSchemas()
        .get("Object4");

    // Assert
    assertTrue(ResponseTemplateBuilder.getXdwsType(schema)
        .isEmpty());
  }

  @Test
  void build_returnsResponseHeaders() {
    // Arrange
    ResponseHeader expectedResponseHeader = ResponseHeader.builder()
        .name("X-Response-Header")
        .dwsExpressionMap(Map.of(X_DWS_EXPR_VALUE, "`value`"))
        .type("string")
        .build();

    // Act
    List<ResponseTemplate> responseTemplates =
        getResponseTemplates(this.openApi, ImmutableList.of("customType"), "/query6", HttpMethod.GET);

    // Assert
    Map<String, ResponseHeader> responseHeaders = responseTemplates.get(0)
        .getResponseHeaders();
    assertEquals(expectedResponseHeader, responseHeaders.get("X-Response-Header"));
  }

  @Test
  void build_returnsResponseHeaders_forHeaderWithRef() {
    // Arrange
    ResponseHeader expectedResponseHeader = ResponseHeader.builder()
        .name("X-Response-Header-Ref")
        .dwsExpressionMap(Map.of(X_DWS_EXPR_VALUE, "`ref`"))
        .type("string")
        .build();

    // Act
    List<ResponseTemplate> responseTemplates =
        getResponseTemplates(this.openApi, ImmutableList.of("customType"), "/query6", HttpMethod.GET);

    // Assert
    Map<String, ResponseHeader> responseHeaders = responseTemplates.get(0)
        .getResponseHeaders();
    assertEquals(expectedResponseHeader, responseHeaders.get("X-Response-Header-Ref"));
  }

  public static List<ResponseTemplate> getResponseTemplates(OpenAPI openApi, String path, HttpMethod httpMethod) {
    return getResponseTemplates(openApi, Collections.emptyList(), path, httpMethod);
  }

  static List<ResponseTemplate> getResponseTemplates(OpenAPI openApi, List<String> xdwsStringTypes, String path,
      HttpMethod httpMethod) {
    Operation operation;
    switch (httpMethod) {
      case GET:
        operation = openApi.getPaths()
            .get(path)
            .getGet();
        break;
      case POST:
        operation = openApi.getPaths()
            .get(path)
            .getPost();
        break;
      default:
        throw ExceptionHelper.unsupportedOperationException("method '{}' not yet supported.", httpMethod);
    }
    return new ResponseTemplateBuilder(openApi, xdwsStringTypes).buildResponseTemplates(HttpMethodOperation.builder()
        .name(path)
        .httpMethod(httpMethod)
        .operation(operation)
        .build());
  }

  @Test
  void build_returns_withArrayDefaultValue() {
    // Act
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query12", HttpMethod.GET);

    // Assert
    assertNotNull(templates);
    assertEquals(1, templates.size());
    ResponseObject responseObject = templates.get(0)
        .getResponseObject();
    assertEquals("query12", responseObject.getIdentifier());
    assertEquals(2, responseObject.getSummary()
        .getChildren()
        .size());
    ResponseObject prop2 = responseObject.getSummary()
        .getChildren()
        .get(1);
    assertEquals("o12_prop2", prop2.getIdentifier());
    assertEquals(OasConstants.ARRAY_TYPE, prop2.getSummary()
        .getType());
    assertTrue(prop2.getSummary()
        .isEnvelope());
    assertEquals(1, prop2.getSummary()
        .getItems()
        .size());
    assertTrue(prop2.getSummary()
        .getItems()
        .get(0)
        .getSummary()
        .isEnvelope());
  }


}
