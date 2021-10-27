package org.dotwebstack.framework.core.graphql.proxy;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import graphql.ExecutionResult;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.condition.GraphQlNativeDisabled;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import reactor.netty.http.client.HttpClient;

@Conditional(GraphQlNativeDisabled.class)
@Configuration
public class GraphQlProxyConfig {

  private final Environment environment;

  public GraphQlProxyConfig(@NonNull Environment environment) {
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
  public HttpClient proxyHttpClient(@NonNull Schema schema) {
    String proxyName = schema.getSettings()
        .getGraphql()
        .getProxy();

    String uri = getProxyEnv(proxyName, "uri")
        .orElseThrow(() -> new InvalidConfigurationException(format("Missing URI config for proxy '%s'.", proxyName)));

    Consumer<HttpHeaders> headerBuilder = headers -> getProxyEnv(proxyName, "bearerAuth")
        .ifPresent(bearerAuth -> headers.add("Authorization", "Bearer ".concat(bearerAuth)));

    return HttpClient.create()
        .baseUrl(uri)
        .headers(headerBuilder);
  }

  private Optional<String> getProxyEnv(String proxyName, String key) {
    return Optional
        .ofNullable(environment.getProperty(String.format("dotwebstack.graphql.proxies.%s.%s", proxyName, key)));
  }
}
