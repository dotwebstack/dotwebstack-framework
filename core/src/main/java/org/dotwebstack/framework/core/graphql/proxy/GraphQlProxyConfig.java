package org.dotwebstack.framework.core.graphql.proxy;

import static java.lang.String.format;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.internalServerErrorException;

import graphql.GraphQL;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.condition.GraphQlNativeDisabled;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.dotwebstack.graphql.orchestrate.schema.RemoteExecutor;
import org.dotwebstack.graphql.orchestrate.schema.SchemaIntrospector;
import org.dotwebstack.graphql.orchestrate.schema.Subschema;
import org.dotwebstack.graphql.orchestrate.wrap.SchemaWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Conditional(GraphQlNativeDisabled.class)
@Configuration
public class GraphQlProxyConfig {

  private final Environment environment;

  public GraphQlProxyConfig(@NonNull Environment environment) {
    this.environment = environment;
  }

  @Bean
  @ConditionalOnMissingBean
  public GraphQL graphql(RemoteExecutor remoteExecutor) {
    TypeDefinitionRegistry typeDefinitionRegistry;

    try {
      typeDefinitionRegistry = SchemaIntrospector.introspectSchema(remoteExecutor)
          .get();
    } catch (InterruptedException | ExecutionException e) {
      throw internalServerErrorException(e);
    }

    var schema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, RuntimeWiring.newRuntimeWiring()
        .scalar(CoreScalars.DATE)
        .scalar(CoreScalars.DATETIME)
        .scalar(CoreScalars.OBJECT)
        .build());

    var wrappedSchema = SchemaWrapper.wrap(Subschema.builder()
        .schema(schema)
        .executor(remoteExecutor)
        .build());

    return GraphQL.newGraphQL(wrappedSchema)
        .build();
  }

  @Bean
  public RemoteExecutor remoteExecutor(DotWebStackConfiguration configuration, WebClient.Builder webClientBuilder) {
    var proxyName = configuration.getSettings()
        .getGraphql()
        .getProxy();

    var endpoint = URI.create(getProxyEnv(proxyName, "uri")
        .orElseThrow(() -> new InvalidConfigurationException(format("Missing URI config for proxy '%s'.", proxyName))));

    Consumer<HttpHeaders> headerBuilder = headers -> getProxyEnv(proxyName, "bearerAuth")
        .ifPresent(bearerAuth -> headers.add("Authorization", "Bearer ".concat(bearerAuth)));

    var webClient = webClientBuilder.defaultHeaders(headerBuilder)
        .build();

    return RemoteExecutor.builder()
        .endpoint(endpoint)
        .webClient(webClient)
        .build();
  }

  private Optional<String> getProxyEnv(String proxyName, String key) {
    return Optional
        .ofNullable(environment.getProperty(String.format("dotwebstack.graphql.proxies.%s.%s", proxyName, key)));
  }
}
