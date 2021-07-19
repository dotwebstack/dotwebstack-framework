package org.dotwebstack.framework.core.graphql.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;


@ExtendWith(MockitoExtension.class)
class GraphqQlProxyConfigTest {
  @Mock
  private Environment env;

  @InjectMocks
  private GraphqQlProxyConfig config;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DotWebStackConfiguration dwsConfig;

  @Test
  void proxyObjectMapper_returnsResult() {
    assertThat(config.proxyObjectMapper(), notNullValue());
  }

  @Test
  void proxyUri_returnsExpectedValue() {
    when(dwsConfig.getSettings()
        .getGraphql()
        .getProxy()).thenReturn("theproxy");
    when(env.getProperty(any(String.class))).thenReturn("theuri");

    String uri = config.proxyUri(dwsConfig);

    assertThat(uri, is("theuri"));
  }

  @Test
  void proxyHttpClient_returnResult() {
    assertThat(config.proxyHttpClient(), notNullValue());
  }
}
