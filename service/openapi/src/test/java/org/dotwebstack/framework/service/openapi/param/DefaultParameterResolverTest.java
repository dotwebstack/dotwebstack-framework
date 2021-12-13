package org.dotwebstack.framework.service.openapi.param;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.parameters.RequestBody;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.ServerRequest;

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

  @Test
  void resolveUrlAndHeaderParameters_givenServerRequest_resolvesExpectedParameters() throws URISyntaxException {
    String base = "http://dotwebstack.org";
    String path = "/breweries";
    String query = "page=2&name=Foo";
    String uriString = String.format("%s%s?%s", base, path, query);
    URI uri = new URI(uriString);

    when(serverRequest.uri()).thenReturn(uri);

    DefaultParameterResolver defaultParameterResolver = new DefaultParameterResolver(requestBody, requestBodyContext,
        requestBodyHandlerRouter, paramHandlerRouter, properties, jexlEngine, List.of(), Map.of());

    Map<String, Object> resolvedParams = defaultParameterResolver.resolveUrlAndHeaderParameters(serverRequest);

    assertThat(resolvedParams.get("requestUri"), is(uriString));
    assertThat(resolvedParams.get("requestPathAndQuery"), is(String.format("%s?%s", path, query)));
  }
}
