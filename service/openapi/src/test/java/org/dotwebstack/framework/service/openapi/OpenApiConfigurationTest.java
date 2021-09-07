package org.dotwebstack.framework.service.openapi;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.graphql.GraphQlService;
import org.dotwebstack.framework.core.templating.TemplateResponseMapper;
import org.dotwebstack.framework.service.openapi.exception.InvalidOpenApiConfigurationException;
import org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.JsonResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContextBuilder;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

    assertEquals(19, optionsAnswer.getResults()
        .size()); // Assert OPTIONS route

    verify(this.openApiConfiguration, times(20)).toRouterFunctions(any(ResponseTemplateBuilder.class),
        any(RequestBodyContextBuilder.class), argumentCaptor.capture());

    List<HttpMethodOperation> actualHttpMethodOperations = argumentCaptor.getAllValues();
    assertEquals(20, actualHttpMethodOperations.size());

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
    assertEquals("/query2", actualHttpMethodOperations.get(2)
        .getName());

    assertEquals(HttpMethod.GET, actualHttpMethodOperations.get(3)
        .getHttpMethod());
    assertEquals("/query3/{query3_param1}", actualHttpMethodOperations.get(3)
        .getName());

    assertEquals(HttpMethod.GET, actualHttpMethodOperations.get(4)
        .getHttpMethod());
    assertEquals("/query4", actualHttpMethodOperations.get(4)
        .getName());

    assertEquals(HttpMethod.GET, actualHttpMethodOperations.get(5)
        .getHttpMethod());
    assertEquals("/query5", actualHttpMethodOperations.get(5)
        .getName());

    assertEquals(HttpMethod.GET, actualHttpMethodOperations.get(6)
        .getHttpMethod());
    assertEquals("/query6", actualHttpMethodOperations.get(6)
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

  @ParameterizedTest(name = "{0}")
  @MethodSource("getPaths")
  void getHttpMethodOperations_returnsHttpOperations_GivenPathItemAndName(String pathName) {
    // Arrange
    PathItem pathItem = openApi.getPaths()
        .get(pathName);

    // Act
    List<HttpMethodOperation> httpMethodOperations = OpenApiConfiguration.getHttpMethodOperations(pathItem, pathName);

    // Assert
    Set<Operation> operations = getOperations(pathItem);

    assertThat(httpMethodOperations.size(), is(operations.size()));

    if (!httpMethodOperations.isEmpty()) {
      assertThat(httpMethodOperations.stream()
          .map(HttpMethodOperation::getName)
          .collect(Collectors.toUnmodifiableSet()), hasItem(pathName));
      assertThat(httpMethodOperations.stream()
          .map(HttpMethodOperation::getOperation)
          .collect(Collectors.toUnmodifiableSet()), is(operations));
    }
  }

  private static Set<String> getPaths() {
    return TestResources.openApi()
        .getPaths()
        .keySet();
  }

  private Set<Operation> getOperations(PathItem pathItem) {
    return Stream.of(pathItem.getGet(), pathItem.getPost())
        .filter(Objects::nonNull)
        .filter(DwsExtensionHelper::isDwsOperation)
        .collect(Collectors.toUnmodifiableSet());
  }
}
