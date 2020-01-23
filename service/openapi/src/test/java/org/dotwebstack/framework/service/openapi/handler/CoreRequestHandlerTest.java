package org.dotwebstack.framework.service.openapi.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.GraphQL;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.exception.NotAcceptableException;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilderTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CoreRequestHandlerTest {

  @Mock
  private ResponseSchemaContext responseSchemaContext;

  @Mock
  private ResponseContextValidator responseContextValidator;

  @Mock
  private GraphQL graphQl;

  @Mock
  private ResponseMapper responseMapper;

  @Mock
  private RequestBodyHandlerRouter requestBodyHandlerRouter;

  @Mock
  private EnvironmentProperties environmentProperties;

  private OpenAPI openApi = TestResources.openApi();

  private ParamHandlerRouter paramHandlerRouter = new ParamHandlerRouter(Collections.emptyList(), this.openApi);

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  private final JexlHelper jexlHelper = new JexlHelper(this.jexlEngine);

  private CoreRequestHandler coreRequestHandler;

  @BeforeEach
  public void setup() throws NotAcceptableException {
    Operation operation = this.openApi.getPaths()
        .get("/query6")
        .getGet();
    ApiResponse apiResponse = operation.getResponses()
        .get("200");
    apiResponse.getContent()
        .get("application/hal+json")
        .getSchema()
        .set$ref("#/components/schemas/Object4");

    when(this.responseSchemaContext.getParameters()).thenReturn(operation.getParameters());

    when(this.responseSchemaContext.getGraphQlField())
        .thenReturn(TestResources.getGraphQlField(TestResources.typeDefinitionRegistry(), "query6"));
    coreRequestHandler = spy(new CoreRequestHandler(openApi, "/query6", responseSchemaContext, responseContextValidator,
        graphQl, responseMapper, paramHandlerRouter, requestBodyHandlerRouter, jexlHelper, environmentProperties));

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);
    doReturn(responseTemplate).when(this.coreRequestHandler)
        .getResponseTemplate(null);
  }

  @Test
  public void handle_ReturnsHeaders() {
    // Act
    coreRequestHandler.handle(getServerRequest())
        .doOnSuccess(response -> {

          // Assert
          assertNotNull(response.headers());
          assertEquals(ImmutableList.of("value"), response.headers()
              .get("X-Response-Header"));
        });
  }

  @ParameterizedTest
  @CsvSource({"application/xml, application/xml", "application/json, application/json"})
  public void getResponseTemplateTest(String acceptHeader, String expected) {
    // Act
    getServerRequest();
    ResponseTemplate responseTemplate = coreRequestHandler.getResponseTemplate(acceptHeader);

    // Assert
    assertEquals(responseTemplate.getMediaType(), expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/json;q=0.9,application/xml", "application/*"})
  public void getResponseTemplateWithQualityAndWildcardTest(String acceptHeader) {
    // Act
    getServerRequest();
    ResponseTemplate responseTemplate = coreRequestHandler.getResponseTemplate(acceptHeader);

    // Assert
    assertEquals(responseTemplate.getMediaType(), "application/xml");
  }

  @ParameterizedTest
  @ValueSource(strings = {"*/*", ""})
  public void getDefaultResponseTemplateWhenNoAcceptHeaderIsProvidedTest(String acceptHeader) {
    // Act
    getServerRequest();
    ResponseTemplate responseTemplate = coreRequestHandler.getResponseTemplate(acceptHeader);

    // Assert
    assertEquals(responseTemplate.getMediaType(), "application/json");
  }

  @Test
  public void shouldThrowNotAcceptedExceptionTest() {
    // Act
    getServerRequest();
    String acceptHeader = "application/not_supported";

    // Assert
    assertThrows(NotAcceptableException.class, () -> coreRequestHandler.getResponseTemplate(acceptHeader));
  }

  @Test
  public void createResponseHeaders_returnsValue_forStaticJexlValue() {
    // Arrange
    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);

    // Act
    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, new HashMap<>());

    // Assert
    assertEquals("value", responseHeaders.get("X-Response-Header"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void createResponseHeaders_returnsValue_forJexlWithArgument() {
    // Arrange
    when(this.environmentProperties.getAllProperties()).thenReturn(ImmutableMap.of("property1", "value1"));

    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getHeaders()
        .get("X-Response-Header")
        .getSchema()
        .getExtensions()
        .put("x-dws-expr", "args.argument1");

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);
    Map<String, Object> inputParams = ImmutableMap.of("argument1", "argument1Value");

    // Act
    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, inputParams);

    // Assert
    assertEquals("argument1Value", responseHeaders.get("X-Response-Header"));

  }

  @SuppressWarnings("unchecked")
  @Test
  public void createResponseHeaders_returnsValue_forJexlWithEnv() {
    // Arrange
    when(this.environmentProperties.getAllProperties()).thenReturn(ImmutableMap.of("property1", "value1"));

    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getHeaders()
        .get("X-Response-Header")
        .getSchema()
        .getExtensions()
        .put("x-dws-expr", "env.property1");

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);

    // Act
    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, new HashMap<>());

    // Assert
    assertEquals("value1", responseHeaders.get("X-Response-Header"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void createResponseHeaders_returnsValue_forDefault() {
    // Arrange
    when(this.environmentProperties.getAllProperties()).thenReturn(ImmutableMap.of("property1", "value1"));

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);

    // Act
    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, new HashMap<>());

    // Assert
    assertEquals("defaultValue", responseHeaders.get("X-Response-Default"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void createResponseHeaders_throwsException_whenNoValueFound() {
    // Arrange
    when(this.environmentProperties.getAllProperties()).thenReturn(ImmutableMap.of("property1", "value1"));

    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getHeaders()
        .get("X-Response-Default")
        .getSchema()
        .setDefault(null);

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);

    // Act & Assert
    assertThrows(JexlException.class,
        () -> coreRequestHandler.createResponseHeaders(responseTemplate, new HashMap<>()));
  }

  @Test
  public void resolveUrlAndHeaderParameters_returnsValue() {
    // Arrange
    ServerRequest request = getServerRequest();

    // Act
    Map<String, Object> params = this.coreRequestHandler.resolveUrlAndHeaderParameters(request);

    // Assert
    assertEquals(1, params.size());
  }

  protected ServerRequest getServerRequest() {
    ServerRequest request = mock(ServerRequest.class);
    ServerRequest.Headers headers = mock(ServerRequest.Headers.class);
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.put("query6_param1", ImmutableList.of("value1"));

    when(responseSchemaContext.getResponses()).thenReturn(getResponseTemplates());
    when(request.headers()).thenReturn(headers);
    when(request.queryParams()).thenReturn(queryParams);
    return request;
  }

  private List<ResponseTemplate> getResponseTemplates() {
    List<ResponseTemplate> responseTemplates = new ArrayList<>();

    responseTemplates.add(ResponseTemplate.builder()
        .mediaType("application/json")
        .isDefault(true)
        .responseCode(200)
        .build());

    responseTemplates.add(ResponseTemplate.builder()
        .mediaType("application/xml")
        .responseCode(200)
        .build());

    return responseTemplates;
  }

}
