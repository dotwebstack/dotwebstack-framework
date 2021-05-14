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
    List<ResponseTemplate> responseTemplates = getResponseTemplates(this.openApi, "/query1", HttpMethod.GET);

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
    openApi.getComponents()
        .getSchemas()
        .remove("Object1");

    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query1", HttpMethod.GET));
  }

  @Test
  void build_throwsException_MissingSchema2() {
    openApi.getComponents()
        .getSchemas()
        .remove("Object2");

    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query1", HttpMethod.GET));
  }

  @Test
  void build_resolvesXdwsTemplate_forValidSchema() {
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query1", HttpMethod.GET);

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
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query7", HttpMethod.GET);

    assertEquals(1, templates.size());
  }

  @Test
  void build_resolvesSchema_forRecursiveArraySchema() {
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query8", HttpMethod.GET);

    assertEquals(1, templates.size());
  }

  @Test
  void build_resolvesAllOfTemplate_forValidSchema() {
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query5", HttpMethod.GET);

    assertEquals(2, templates.size());
    ResponseTemplate responseTemplate = templates.get(0);
    assertEquals(2, responseTemplate.getResponseObject()
        .getSummary()
        .getComposedOf()
        .size());
  }

  @Test
  @SuppressWarnings("rawtypes")
  void build_throwsException_ObjectXdwsTemplateType() {
    Schema<?> property1 = (Schema) openApi.getComponents()
        .getSchemas()
        .get("Object1")
        .getProperties()
        .get("o1_prop1");
    property1.setType("object");

    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query1", HttpMethod.GET));
  }

  @Test
  @SuppressWarnings("rawtypes")
  void build_throwsException_IntegerXdwsTemplateType() {
    Schema<?> property1 = (Schema) openApi.getComponents()
        .getSchemas()
        .get("Object1")
        .getProperties()
        .get("o1_prop1");
    property1.setType("integer");

    assertDoesNotThrow(() -> getResponseTemplates(this.openApi, "/query1", HttpMethod.GET));
  }

  @Test
  void build_throwsException_MissingXDwsExprInHeaderSchema() {
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

    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query6", HttpMethod.GET));
  }

  @Test
  @SuppressWarnings("unchecked")
  void build_throwsException_MissingExtensionsInHeaderSchema() {
    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getHeaders()
        .get("X-Response-Header")
        .getSchema()
        .setExtensions(null);

    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, "/query6", HttpMethod.GET));
  }

  @Test
  void build_throwsException_without_configuredXdwsStringType() {
    List<String> xdwsStringTypes = ImmutableList.of("unknown type");
    assertThrows(InvalidConfigurationException.class,
        () -> getResponseTemplates(this.openApi, xdwsStringTypes, "/query6", HttpMethod.GET));
  }

  @Test
  void build_succeeds_with_configuredXdwsStringType() {
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
    Schema<?> schema = this.openApi.getComponents()
        .getSchemas()
        .get("Object5");

    Optional<String> xdwsType = ResponseTemplateBuilder.getXdwsType(schema);

    assertTrue(xdwsType.isPresent());
    assertEquals("customType", xdwsType.get());
  }

  @Test
  void getXdwsType_returns_empty() {
    Schema<?> schema = this.openApi.getComponents()
        .getSchemas()
        .get("Object4");

    assertTrue(ResponseTemplateBuilder.getXdwsType(schema)
        .isEmpty());
  }

  @Test
  void build_returnsResponseHeaders() {
    ResponseHeader expectedResponseHeader = ResponseHeader.builder()
        .name("X-Response-Header")
        .dwsExpressionMap(Map.of(X_DWS_EXPR_VALUE, "`value`"))
        .type("string")
        .build();

    List<ResponseTemplate> responseTemplates =
        getResponseTemplates(this.openApi, ImmutableList.of("customType"), "/query6", HttpMethod.GET);

    Map<String, ResponseHeader> responseHeaders = responseTemplates.get(0)
        .getResponseHeaders();
    assertEquals(expectedResponseHeader, responseHeaders.get("X-Response-Header"));
  }

  @Test
  void build_returnsResponseHeaders_forHeaderWithRef() {
    ResponseHeader expectedResponseHeader = ResponseHeader.builder()
        .name("X-Response-Header-Ref")
        .dwsExpressionMap(Map.of(X_DWS_EXPR_VALUE, "`ref`"))
        .type("string")
        .build();

    List<ResponseTemplate> responseTemplates =
        getResponseTemplates(this.openApi, ImmutableList.of("customType"), "/query6", HttpMethod.GET);

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
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query12", HttpMethod.GET);

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
        .isTransient());
    assertEquals(1, prop2.getSummary()
        .getItems()
        .size());
    assertTrue(prop2.getSummary()
        .getItems()
        .get(0)
        .getSummary()
        .isTransient());
  }
}
