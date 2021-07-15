package org.dotwebstack.framework.core.graphql.proxy;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import graphql.ExecutionResult;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.condition.GraphQlNativeDisabled;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import reactor.netty.http.client.HttpClient;

@Conditional(GraphQlNativeDisabled.class)
@Configuration
public class GraphqQlProxyConfig {

  private final Environment environment;

  public GraphqQlProxyConfig(@NonNull Environment environment) {
    this.environment = environment;
  }

  @Bean
  public ObjectMapper proxyObjectMapper() {
    ObjectMapper result = new ObjectMapper();
    SimpleModule sm = new SimpleModule("Graphql");
    sm.addDeserializer(ExecutionResult.class, new ExecutionResultDeserializer(ExecutionResult.class));
    result.registerModule(sm);
    return result;
  }

  @Bean
  public String proxyUri(@NonNull DotWebStackConfiguration dotWebStackConfiguration) {
    String proxyname = dotWebStackConfiguration.getSettings()
        .getGraphql()
        .getProxy();
    String uriKey = "dotwebstack.graphql.proxies." + proxyname + ".uri";
    String uri = environment.getProperty(uriKey);
    if (uri == null) {
      throw new InvalidConfigurationException(format("Missing property %s", uriKey));
    }
    return uri;
  }

  @Bean
  public HttpClient proxyHttpClient() {
    return HttpClient.create();
  }
}
