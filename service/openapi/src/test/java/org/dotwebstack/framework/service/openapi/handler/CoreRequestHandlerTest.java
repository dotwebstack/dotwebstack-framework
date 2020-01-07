package org.dotwebstack.framework.service.openapi.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.GraphQL;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.TestResources;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

@ExtendWith(MockitoExtension.class)
public class CoreRequestHandlerTest {

  private OpenAPI openApi = TestResources.openApi();

  @Mock
  private ResponseSchemaContext responseSchemaContext;

  @Mock
  private ResponseContextValidator responseContextValidator;

  @Mock
  private GraphQL graphQl;

  @Mock
  private ResponseMapper responseMapper;

  @Mock
  private ParamHandlerRouter paramHandlerRouter;

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  private final JexlHelper jexlHelper = new JexlHelper(this.jexlEngine);

  @Mock
  private RequestBodyHandlerRouter requestBodyHandlerRouter;

  @Mock
  private EnvironmentProperties environmentProperties;

  private CoreRequestHandler coreRequestHandler;

  @BeforeEach
  public void setup() {
    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getContent()
        .get("application/hal+json")
        .getSchema()
        .set$ref("#/components/schemas/Object4");

    when(this.responseSchemaContext.getGraphQlField())
        .thenReturn(TestResources.getGraphQlField(TestResources.typeDefinitionRegistry(), "query6"));
    coreRequestHandler = spy(new CoreRequestHandler(openApi, "/query6", responseSchemaContext, responseContextValidator,
        graphQl, responseMapper, paramHandlerRouter, requestBodyHandlerRouter, jexlHelper, environmentProperties));
  }

  @Test
  public void handle_ReturnsHeaders() {
    doReturn(new HashMap<>()).when(this.coreRequestHandler)
        .resolveUrlAndHeaderParameters(any());
    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);
    doReturn(responseTemplate).when(this.coreRequestHandler)
        .getResponseTemplate();

    ServerResponse response = coreRequestHandler.handle(mock(ServerRequest.class))
        .block();
    assertNotNull(response.headers());
    assertEquals(ImmutableList.of("value"), response.headers()
        .get("X-Response-Header"));
  }

  @Test
  public void createResponseHeaders_returnsValue_forStaticJexlValue() {
    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);
    Map<String, Object> inputParams = new HashMap<>();
    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, inputParams);
    assertEquals("value", responseHeaders.get("X-Response-Header"));

  }

  @SuppressWarnings("unchecked")
  @Test
  public void createResponseHeaders_returnsValue_forJexlWithArgument() {
    CoreRequestHandler coreRequestHandler =
        spy(new CoreRequestHandler(openApi, "/query6", responseSchemaContext, responseContextValidator, graphQl,
            responseMapper, paramHandlerRouter, requestBodyHandlerRouter, jexlHelper, environmentProperties));

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
    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, inputParams);
    assertEquals("argument1Value", responseHeaders.get("X-Response-Header"));

  }

  @SuppressWarnings("unchecked")
  @Test
  public void createResponseHeaders_returnsValue_forJexlWithEnv() {

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
    Map<String, Object> inputParams = new HashMap<>();
    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, inputParams);
    assertEquals("value1", responseHeaders.get("X-Response-Header"));
  }
}
