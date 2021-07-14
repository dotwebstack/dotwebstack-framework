package org.dotwebstack.framework.core.graphql;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import graphql.ExecutionResult;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.condition.GraphQlNativeDisabled;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.graphql.client.ExecutionResultDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Conditional(GraphQlNativeDisabled.class)
@Configuration
public class ProxyConfig {

  private final Environment environment;

  public ProxyConfig(@NonNull Environment environment) {
    this.environment = environment;
  }

  @Bean
  public ObjectMapper proxyObjectMapper() {
    ObjectMapper result = new ObjectMapper();
    SimpleModule sm = new SimpleModule("GraphqlResult");
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
}
