package org.dotwebstack.framework.service.openapi;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.GraphQL;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.requestbody.DefaultRequestBodyHandler;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContextBuilder;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@ExtendWith(MockitoExtension.class)
public class OpenApiConfigurationTest {

  @Mock
  private GraphQL graphQL;

  private TypeDefinitionRegistry registry;

  private OpenAPI openApi;

  private InputStream openApiStream;

  private OpenApiConfiguration openApiConfiguration;

  @Mock
  private ResponseContextValidator responseContextValidator;

  @Mock
  private ResponseMapper responseMapper;

  @Mock
  private RequestBodyHandlerRouter requestBodyHandlerRouter;

  @Mock
  private JexlEngine jexlEngine;

  @Mock
  private EnvironmentProperties environmentProperties;

  @BeforeEach
  public void setup() {
    this.registry = TestResources.typeDefinitionRegistry();
    this.openApi = TestResources.openApi();
    this.openApiStream = TestResources.openApiStream();
    OpenApiProperties openApiProperties = new OpenApiProperties();
    openApiProperties.setXdwsStringTypes(List.of("customType"));
    this.openApiConfiguration = spy(new OpenApiConfiguration(openApi, graphQL, this.registry, responseMapper,
        new ParamHandlerRouter(Collections.emptyList(), openApi), openApiStream, responseContextValidator,
        requestBodyHandlerRouter, openApiProperties, jexlEngine, environmentProperties));
  }

  @Test
  public void route_returnsFunctions() {
    // Arrange
    final ArgumentCaptor<HttpMethodOperation> argumentCaptor = ArgumentCaptor.forClass(HttpMethodOperation.class);

    final RouterFunctionAnswer optionsAnswer = new RouterFunctionAnswer();
    doAnswer(optionsAnswer).when(openApiConfiguration)
        .toOptionRouterFunction(anyList());

    when(requestBodyHandlerRouter.getRequestBodyHandler(any()))
        .thenReturn(new DefaultRequestBodyHandler(this.openApi, this.registry, new Jackson2ObjectMapperBuilder()));

    // Act
    openApiConfiguration.route(openApi);

    // Assert
    assertEquals(6, optionsAnswer.getResults()
        .size()); // Assert OPTIONS route

    verify(this.openApiConfiguration, times(7)).toRouterFunctions(any(ResponseTemplateBuilder.class),
        any(RequestBodyContextBuilder.class), argumentCaptor.capture());

    List<HttpMethodOperation> actualHttpMethodOperations = argumentCaptor.getAllValues();
    assertEquals(7, actualHttpMethodOperations.size());

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
  public void route_throwsException_MissingQuery() {
    // Arrange
    openApi.getPaths()
        .get("/query1")
        .getGet()
        .getExtensions()
        .put(X_DWS_QUERY, "unknownQuery");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> openApiConfiguration.route(openApi));
  }

  @SuppressWarnings("unchecked")
  private static class RouterFunctionAnswer implements Answer<Optional<RouterFunction<ServerResponse>>> {

    @Getter
    private List<RouterFunction<ServerResponse>> results = new ArrayList<>();

    @Override
    public Optional<RouterFunction<ServerResponse>> answer(InvocationOnMock invocationOnMock) throws Throwable {
      Optional<RouterFunction<ServerResponse>> result =
          (Optional<RouterFunction<ServerResponse>>) invocationOnMock.callRealMethod();
      result.ifPresent(results::add);
      return result;
    }
  }
}
