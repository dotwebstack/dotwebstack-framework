package org.dotwebstack.framework.service.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.swagger.v3.oas.models.OpenAPI;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.graphql.GraphQlService;
import org.dotwebstack.framework.core.templating.TemplateResponseMapper;
import org.dotwebstack.framework.service.openapi.exception.InvalidOpenApiConfigurationException;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.JsonResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@ExtendWith(MockitoExtension.class)
class OpenApiConfigurationTest {

  @Mock
  private GraphQlService graphQL;

  private OpenAPI openApi;

  private InputStream openApiStream;

  private OpenApiConfiguration openApiConfiguration;

  @Mock
  private JsonResponseMapper jsonResponseMapper;

  @Mock
  private TemplateResponseMapper templateResponseMapper;

  @Mock
  private RequestBodyHandlerRouter requestBodyHandlerRouter;

  @Mock
  private JexlEngine jexlEngine;

  @Mock
  private EnvironmentProperties environmentProperties;

  @Mock
  private GraphQlQueryBuilder graphQlQueryBuilder;

  @BeforeEach
  void setup() {
    this.openApi = TestResources.openApi();
    this.openApiStream = TestResources.openApiStream();

    OpenApiConfiguration apiConfiguration = new OpenApiConfiguration(openApi, graphQL, new ArrayList<>(),
        jsonResponseMapper, new ParamHandlerRouter(Collections.emptyList(), openApi), openApiStream,
        Collections.singletonList(templateResponseMapper), requestBodyHandlerRouter, getOpenApiProperties(), jexlEngine,
        environmentProperties, graphQlQueryBuilder);

    initOpenApiConfiguration(apiConfiguration);
  }

  private void initOpenApiConfiguration(OpenApiConfiguration openApiConfiguration) {
    this.openApiConfiguration = spy(openApiConfiguration);
  }

  private OpenApiProperties getOpenApiProperties() {
    OpenApiProperties openApiProperties = new OpenApiProperties();
    openApiProperties.setXdwsStringTypes(List.of("customType"));
    return openApiProperties;
  }

  @Test
  void route_ThrowsException_InvalidOpenApiConfigurationException() {
    OpenApiConfiguration apiConfiguration =
        new OpenApiConfiguration(openApi, graphQL, new ArrayList<>(), jsonResponseMapper,
            new ParamHandlerRouter(Collections.emptyList(), openApi), openApiStream, Collections.emptyList(),
            requestBodyHandlerRouter, getOpenApiProperties(), jexlEngine, environmentProperties, graphQlQueryBuilder);

    initOpenApiConfiguration(apiConfiguration);

    final RouterFunctionAnswer optionsAnswer = new RouterFunctionAnswer();
    doAnswer(optionsAnswer).when(openApiConfiguration)
        .toOptionRouterFunction(anyList());

    assertThrows(InvalidOpenApiConfigurationException.class, () -> openApiConfiguration.route(openApi));
  }

  @Test
  void test_toOptionRouterFunction() {
    OpenApiConfiguration apiConfiguration = new OpenApiConfiguration(openApi, graphQL, new ArrayList<>(),
        jsonResponseMapper, new ParamHandlerRouter(Collections.emptyList(), openApi), openApiStream,
        Arrays.asList(templateResponseMapper, null), requestBodyHandlerRouter, getOpenApiProperties(), jexlEngine,
        environmentProperties, graphQlQueryBuilder);

    Optional<RouterFunction<ServerResponse>> response =
        apiConfiguration.toOptionRouterFunction(Collections.emptyList());

    assertTrue(response.isEmpty());
  }

  @Test
  void test_toOptionRouterFunction_WithNullArgument() {
    OpenApiConfiguration apiConfiguration = new OpenApiConfiguration(openApi, graphQL, new ArrayList<>(),
        jsonResponseMapper, new ParamHandlerRouter(Collections.emptyList(), openApi), openApiStream,
        Arrays.asList(templateResponseMapper, null), requestBodyHandlerRouter, getOpenApiProperties(), jexlEngine,
        environmentProperties, graphQlQueryBuilder);

    Optional<RouterFunction<ServerResponse>> response = apiConfiguration.toOptionRouterFunction(null);

    assertTrue(response.isEmpty());
  }

  @Test
  void route_ThrowsException_InvalidConfigurationException() {
    OpenApiConfiguration apiConfiguration = new OpenApiConfiguration(openApi, graphQL, new ArrayList<>(),
        jsonResponseMapper, new ParamHandlerRouter(Collections.emptyList(), openApi), openApiStream,
        Arrays.asList(templateResponseMapper, null), requestBodyHandlerRouter, getOpenApiProperties(), jexlEngine,
        environmentProperties, graphQlQueryBuilder);

    initOpenApiConfiguration(apiConfiguration);

    final RouterFunctionAnswer optionsAnswer = new RouterFunctionAnswer();
    doAnswer(optionsAnswer).when(openApiConfiguration)
        .toOptionRouterFunction(anyList());

    assertThrows(InvalidConfigurationException.class, () -> openApiConfiguration.route(openApi));
  }

  @Test
  void test_staticResourceRouter() {
    OpenApiConfiguration apiConfiguration = new OpenApiConfiguration(openApi, graphQL, new ArrayList<>(),
        jsonResponseMapper, new ParamHandlerRouter(Collections.emptyList(), openApi), openApiStream,
        Arrays.asList(templateResponseMapper, null), requestBodyHandlerRouter, getOpenApiProperties(), jexlEngine,
        environmentProperties, graphQlQueryBuilder);

    Optional<RouterFunction<ServerResponse>> result = apiConfiguration.staticResourceRouter();

    assertTrue(result.isPresent());
  }

  @Test
  void route_returnsFunctions() {
    final ArgumentCaptor<HttpMethodOperation> argumentCaptor = ArgumentCaptor.forClass(HttpMethodOperation.class);

    final RouterFunctionAnswer optionsAnswer = new RouterFunctionAnswer();
    doAnswer(optionsAnswer).when(openApiConfiguration)
        .toOptionRouterFunction(anyList());

    openApiConfiguration.route(openApi);

    assertEquals(16, optionsAnswer.getResults()
        .size()); // Assert OPTIONS route

    verify(this.openApiConfiguration, times(17)).toRouterFunctions(argumentCaptor.capture(),
        any(ResponseSchemaContext.class));

    List<HttpMethodOperation> actualHttpMethodOperations = argumentCaptor.getAllValues()
        .stream()
        .sorted((h1, h2) -> h1.getName()
            .equals(h2.getName())
                ? h1.getHttpMethod()
                    .name()
                    .compareTo(h2.getHttpMethod()
                        .name())
                : h1.getName()
                    .compareTo(h2.getName()))
        .collect(Collectors.toList());
    assertEquals(17, actualHttpMethodOperations.size());

    assertEquals(HttpMethod.GET, actualHttpMethodOperations.get(0)
        .getHttpMethod());
    assertEquals("/query1", actualHttpMethodOperations.get(0)
        .getName());

    assertEquals(HttpMethod.POST, actualHttpMethodOperations.get(1)
        .getHttpMethod());
    assertEquals("/query1", actualHttpMethodOperations.get(1)
        .getName());

    assertEquals(HttpMethod.GET, actualHttpMethodOperations.get(2)
        .getHttpMethod());
    assertEquals("/query10", actualHttpMethodOperations.get(2)
        .getName());

    assertEquals(HttpMethod.POST, actualHttpMethodOperations.get(3)
        .getHttpMethod());
    assertEquals("/query11", actualHttpMethodOperations.get(3)
        .getName());

    assertEquals(HttpMethod.GET, actualHttpMethodOperations.get(4)
        .getHttpMethod());
    assertEquals("/query12", actualHttpMethodOperations.get(4)
        .getName());

    assertEquals(HttpMethod.GET, actualHttpMethodOperations.get(5)
        .getHttpMethod());
    assertEquals("/query14", actualHttpMethodOperations.get(5)
        .getName());

    assertEquals(HttpMethod.GET, actualHttpMethodOperations.get(6)
        .getHttpMethod());
    assertEquals("/query15", actualHttpMethodOperations.get(6)
        .getName());
  }

  @Test
  void route_returnsApiDoc_OnBasePathByDefault() {
    RouterFunction<ServerResponse> functions = openApiConfiguration.route(openApi);
    WebTestClient client = WebTestClient.bindToRouterFunction(functions)
        .build();

    client.options()
        .uri("")
        .exchange()
        .expectStatus()
        .isOk();

    client.get()
        .uri("")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .returnResult()
        .getResponseBody()
        .toString()
        .startsWith("openapi: \"3.0.2\"");
  }

  @Test
  void route_returnsApiDoc_OnConfiguredPath() {
    String apiDocPublicationPath = "/openapi.yaml";
    OpenApiProperties openApiProperties = getOpenApiProperties();
    openApiProperties.setApiDocPublicationPath(apiDocPublicationPath);

    OpenApiConfiguration apiConfiguration = new OpenApiConfiguration(openApi, graphQL, new ArrayList<>(),
        jsonResponseMapper, new ParamHandlerRouter(Collections.emptyList(), openApi), openApiStream,
        Collections.singletonList(templateResponseMapper), requestBodyHandlerRouter, openApiProperties, jexlEngine,
        environmentProperties, graphQlQueryBuilder);

    initOpenApiConfiguration(apiConfiguration);

    RouterFunction<ServerResponse> functions = openApiConfiguration.route(openApi);
    WebTestClient client = WebTestClient.bindToRouterFunction(functions)
        .build();

    client.options()
        .uri(apiDocPublicationPath)
        .exchange()
        .expectStatus()
        .isOk();

    client.get()
        .uri(apiDocPublicationPath)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .returnResult()
        .getResponseBody()
        .toString()
        .startsWith("openapi: \"3.0.2\"");
  }

  @SuppressWarnings("unchecked")
  private static class RouterFunctionAnswer implements Answer<Optional<RouterFunction<ServerResponse>>> {

    @Getter
    private final List<RouterFunction<ServerResponse>> results = new ArrayList<>();

    @Override
    public Optional<RouterFunction<ServerResponse>> answer(InvocationOnMock invocationOnMock) throws Throwable {
      Optional<RouterFunction<ServerResponse>> result =
          (Optional<RouterFunction<ServerResponse>>) invocationOnMock.callRealMethod();
      result.ifPresent(results::add);
      return result;
    }
  }
}
