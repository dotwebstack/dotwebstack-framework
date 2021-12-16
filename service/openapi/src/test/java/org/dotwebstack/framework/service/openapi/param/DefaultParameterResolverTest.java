package org.dotwebstack.framework.service.openapi.param;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.requestbody.DefaultRequestBodyHandler;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DefaultParameterResolverTest {

  @Mock
  private RequestBody requestBody;

  @Mock
  private RequestBodyContext requestBodyContext;

  @Mock
  private RequestBodyHandlerRouter requestBodyHandlerRouter;

  @Mock
  private ParamHandlerRouter paramHandlerRouter;

  @Mock
  private EnvironmentProperties properties;

  @Mock
  private JexlEngine jexlEngine;

  @Mock
  private ServerRequest serverRequest;

  @Mock
  private PathParameter pathParameter;

  @Mock
  private QueryParameter queryParameter;

  @Mock
  private DefaultParamHandler defaultParamHandler;

  @Mock
  private DefaultRequestBodyHandler defaultRequestBodyHandler;

  @Test
  void resolveUrlAndHeaderParameters_givenServerRequestAndParams_resolvesExpectedParameters()
      throws URISyntaxException {
    var base = "http://dotwebstack.org";
    var path = "/breweries";
    var query = "page=2&name=Foo";
    var uriString = String.format("%s%s?%s", base, path, query);
    var uri = new URI(uriString);

    var pathParamName = "pathParam";
    var pathValue = "pathValue";
    when(serverRequest.uri()).thenReturn(uri);
    when(serverRequest.pathVariables()).thenReturn(Map.of(pathParamName, pathValue));
    when(pathParameter.getName()).thenReturn(pathParamName);
    when(pathParameter.getIn()).thenReturn("path");
    when(paramHandlerRouter.getParamHandler(pathParameter)).thenReturn(defaultParamHandler);
    when(defaultParamHandler.getParameterName(pathParameter)).thenReturn(pathParamName);
    when(defaultParamHandler.getValue(serverRequest, pathParameter)).thenReturn(Optional.of(pathValue));

    var queryParamName = "queryParam";
    var queryValue = "queryValue";
    when(serverRequest.queryParams())
        .thenReturn(CollectionUtils.toMultiValueMap(Map.of(queryParamName, List.of(queryValue))));
    when(queryParameter.getName()).thenReturn(queryParamName);
    when(queryParameter.getIn()).thenReturn("query");
    when(paramHandlerRouter.getParamHandler(queryParameter)).thenReturn(defaultParamHandler);
    when(defaultParamHandler.getParameterName(queryParameter)).thenReturn(queryParamName);
    when(defaultParamHandler.getValue(serverRequest, queryParameter)).thenReturn(Optional.of(queryValue));

    DefaultParameterResolver defaultParameterResolver =
        new DefaultParameterResolver(requestBody, requestBodyContext, requestBodyHandlerRouter, paramHandlerRouter,
            properties, jexlEngine, List.of(pathParameter, queryParameter), Map.of());

    Map<String, Object> resolvedParams = defaultParameterResolver.resolveUrlAndHeaderParameters(serverRequest);

    assertThat(resolvedParams.get("requestUri"), is(uriString));
    assertThat(resolvedParams.get("requestPathAndQuery"), is(String.format("%s?%s", path, query)));
    assertThat(resolvedParams.get(pathParamName), is(pathValue));
    assertThat(resolvedParams.get(queryParamName), is(queryValue));
  }

  @Test
  void resolveUrlAndHeaderParameters_givenServerRequestAndParamsWithDuplicateNames_throwsException()
      throws URISyntaxException {
    var base = "http://dotwebstack.org";
    var path = "/breweries";
    var query = "page=2&name=Foo";
    var uriString = String.format("%s%s?%s", base, path, query);
    var uri = new URI(uriString);

    var pathParamName = "same";
    var pathValue = "pathValue";
    when(serverRequest.uri()).thenReturn(uri);
    when(serverRequest.pathVariables()).thenReturn(Map.of(pathParamName, pathValue));
    when(pathParameter.getName()).thenReturn(pathParamName);
    when(pathParameter.getIn()).thenReturn("path");
    when(paramHandlerRouter.getParamHandler(pathParameter)).thenReturn(defaultParamHandler);
    when(defaultParamHandler.getParameterName(pathParameter)).thenReturn(pathParamName);
    when(defaultParamHandler.getValue(serverRequest, pathParameter)).thenReturn(Optional.of(pathValue));

    var queryParamName = "same";
    var queryValue = "queryValue";
    when(serverRequest.queryParams())
        .thenReturn(CollectionUtils.toMultiValueMap(Map.of(queryParamName, List.of(queryValue))));
    when(queryParameter.getName()).thenReturn(queryParamName);
    when(queryParameter.getIn()).thenReturn("query");
    when(paramHandlerRouter.getParamHandler(queryParameter)).thenReturn(defaultParamHandler);
    when(defaultParamHandler.getParameterName(queryParameter)).thenReturn(queryParamName);
    when(defaultParamHandler.getValue(serverRequest, queryParameter)).thenReturn(Optional.of(queryValue));

    DefaultParameterResolver defaultParameterResolver =
        new DefaultParameterResolver(requestBody, requestBodyContext, requestBodyHandlerRouter, paramHandlerRouter,
            properties, jexlEngine, List.of(pathParameter, queryParameter), Map.of());

    var exception = assertThrows(InvalidConfigurationException.class,
        () -> defaultParameterResolver.resolveUrlAndHeaderParameters(serverRequest));

    assertThat(exception.getMessage(), is("Encountered duplicate parameter name `same` at `queryParameter`."));
  }

  @Test
  void resolveParameters_givenServerRequestAndParams_resolvesExpectedParameters() throws URISyntaxException {
    var base = "http://dotwebstack.org";
    var path = "/breweries";
    var query = "page=2&name=Foo";
    var uriString = String.format("%s%s?%s", base, path, query);
    var uri = new URI(uriString);

    var pathParamName = "pathParam";
    var pathValue = "pathValue";
    when(serverRequest.uri()).thenReturn(uri);
    when(serverRequest.pathVariables()).thenReturn(Map.of(pathParamName, pathValue));
    when(pathParameter.getName()).thenReturn(pathParamName);
    when(pathParameter.getIn()).thenReturn("path");
    when(paramHandlerRouter.getParamHandler(pathParameter)).thenReturn(defaultParamHandler);
    when(defaultParamHandler.getParameterName(pathParameter)).thenReturn(pathParamName);
    when(defaultParamHandler.getValue(serverRequest, pathParameter)).thenReturn(Optional.of(pathValue));

    var queryParamName = "queryParam";
    var queryValue = "queryValue";
    when(serverRequest.queryParams())
        .thenReturn(CollectionUtils.toMultiValueMap(Map.of(queryParamName, List.of(queryValue))));
    when(queryParameter.getName()).thenReturn(queryParamName);
    when(queryParameter.getIn()).thenReturn("query");
    when(paramHandlerRouter.getParamHandler(queryParameter)).thenReturn(defaultParamHandler);
    when(defaultParamHandler.getParameterName(queryParameter)).thenReturn(queryParamName);
    when(defaultParamHandler.getValue(serverRequest, queryParameter)).thenReturn(Optional.of(queryValue));

    when(requestBodyHandlerRouter.getRequestBodyHandler(requestBody)).thenReturn(defaultRequestBodyHandler);

    var body = Map.<String, Object>of("body", Map.of("foo", "bar"));
    when(defaultRequestBodyHandler.getValues(any(), any(), any(), any())).thenReturn(Mono.just(body));

    DefaultParameterResolver defaultParameterResolver =
        new DefaultParameterResolver(requestBody, requestBodyContext, requestBodyHandlerRouter, paramHandlerRouter,
            properties, jexlEngine, List.of(pathParameter, queryParameter), Map.of());

    Mono<Map<String, Object>> resolvedParams = defaultParameterResolver.resolveParameters(serverRequest);

    StepVerifier.create(resolvedParams)
        .assertNext(params -> assertThat(params.containsKey("body"), is(true)))
        .verifyComplete();
  }

  @Test
  void resolveParameters_givenServerRequestAndParamsAndBodyWithDuplicateNames_throwsException()
      throws URISyntaxException {
    var base = "http://dotwebstack.org";
    var path = "/breweries";
    var query = "page=2&name=Foo";
    var uriString = String.format("%s%s?%s", base, path, query);
    var uri = new URI(uriString);

    var pathParamName = "pathParam";
    var pathValue = "pathValue";
    when(serverRequest.uri()).thenReturn(uri);
    when(serverRequest.pathVariables()).thenReturn(Map.of(pathParamName, pathValue));
    when(pathParameter.getName()).thenReturn(pathParamName);
    when(pathParameter.getIn()).thenReturn("path");
    when(paramHandlerRouter.getParamHandler(pathParameter)).thenReturn(defaultParamHandler);
    when(defaultParamHandler.getParameterName(pathParameter)).thenReturn(pathParamName);
    when(defaultParamHandler.getValue(serverRequest, pathParameter)).thenReturn(Optional.of(pathValue));

    var queryParamName = "same";
    var queryValue = "queryValue";
    when(serverRequest.queryParams())
        .thenReturn(CollectionUtils.toMultiValueMap(Map.of(queryParamName, List.of(queryValue))));
    when(queryParameter.getName()).thenReturn(queryParamName);
    when(queryParameter.getIn()).thenReturn("query");
    when(paramHandlerRouter.getParamHandler(queryParameter)).thenReturn(defaultParamHandler);
    when(defaultParamHandler.getParameterName(queryParameter)).thenReturn(queryParamName);
    when(defaultParamHandler.getValue(serverRequest, queryParameter)).thenReturn(Optional.of(queryValue));

    when(requestBodyHandlerRouter.getRequestBodyHandler(requestBody)).thenReturn(defaultRequestBodyHandler);

    var body = Map.<String, Object>of("same", Map.of("foo", "bar"));
    when(defaultRequestBodyHandler.getValues(any(), any(), any(), any())).thenReturn(Mono.just(body));

    DefaultParameterResolver defaultParameterResolver =
        new DefaultParameterResolver(requestBody, requestBodyContext, requestBodyHandlerRouter, paramHandlerRouter,
            properties, jexlEngine, List.of(pathParameter, queryParameter), Map.of());

    Mono<Map<String, Object>> resolvedParams = defaultParameterResolver.resolveParameters(serverRequest);

    StepVerifier.create(resolvedParams)
        .expectErrorMessage("Request body name `same` already used in parameter `queryValue`.")
        .verify();
  }
}
