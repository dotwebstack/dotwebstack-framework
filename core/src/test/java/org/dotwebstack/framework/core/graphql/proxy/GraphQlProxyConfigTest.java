package org.dotwebstack.framework.core.graphql.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.model.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientConfig;

@ExtendWith(MockitoExtension.class)
class GraphQlProxyConfigTest {

  private static final String PROXY_URI = "http://localhost:8080";

  private static final String PROXY_BEARER_AUTH = "foo";

  @Mock
  private Environment env;

  @InjectMocks
  private GraphQlProxyConfig config;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Schema schema;

  @Test
  void proxyObjectMapper_returnsResult() {
    assertThat(config.proxyObjectMapper(), notNullValue());
  }

  @Test
  void proxyHttpClient_returnsClient_forPresentEnvConfig() {
    when(schema.getSettings()
        .getGraphql()
        .getProxy()).thenReturn("theproxy");
    when(env.getProperty("dotwebstack.graphql.proxies.theproxy.uri")).thenReturn(PROXY_URI);

    HttpClient httpClient = config.proxyHttpClient(schema);
    assertThat(httpClient, notNullValue());

    HttpClientConfig httpClientConfig = httpClient.configuration();
    assertThat(httpClientConfig.baseUrl(), is(PROXY_URI));
    assertThat(httpClientConfig.headers()
        .isEmpty(), is(true));
  }

  @Test
  void proxyHttpClient_throwsException_forAbsentEnvConfig() {
    lenient().when(schema.getSettings()
        .getGraphql()
        .getProxy())
        .thenReturn("theproxy");

    assertThrows(InvalidConfigurationException.class, () -> config.proxyHttpClient(schema));
  }

  @Test
  void proxyHttpClient_returnsClientWithAuthHeader_whenBearerAuthEnabled() {
    when(schema.getSettings()
        .getGraphql()
        .getProxy()).thenReturn("theproxy");
    when(env.getProperty("dotwebstack.graphql.proxies.theproxy.uri")).thenReturn(PROXY_URI);
    when(env.getProperty("dotwebstack.graphql.proxies.theproxy.bearerAuth")).thenReturn(PROXY_BEARER_AUTH);

    HttpClient httpClient = config.proxyHttpClient(schema);
    assertThat(httpClient, notNullValue());

    HttpClientConfig httpClientConfig = httpClient.configuration();
    assertThat(httpClientConfig.baseUrl(), is(PROXY_URI));
    assertThat(httpClientConfig.headers()
        .isEmpty(), is(false));
    assertThat(httpClientConfig.headers()
        .get("Authorization"), is("Bearer ".concat(PROXY_BEARER_AUTH)));
  }
}
