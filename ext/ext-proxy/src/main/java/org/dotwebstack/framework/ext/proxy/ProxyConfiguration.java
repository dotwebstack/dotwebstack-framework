package org.dotwebstack.framework.ext.proxy;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.internalServerErrorException;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Optional;
import java.util.function.Consumer;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.dotwebstack.graphql.orchestrate.schema.RemoteExecutor;
import org.dotwebstack.graphql.orchestrate.schema.SchemaIntrospector;
import org.dotwebstack.graphql.orchestrate.schema.Subschema;
import org.dotwebstack.graphql.orchestrate.wrap.SchemaWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class ProxyConfiguration {

  private final ProxyConfigurationProperties configurationProperties;

  private final WebClient.Builder webClientBuilder;

  public ProxyConfiguration(ProxyConfigurationProperties configurationProperties, WebClient.Builder webClientBuilder) {
    this.configurationProperties = configurationProperties;
    this.webClientBuilder = webClientBuilder;
  }

  @Bean
  @Primary
  public GraphQLSchema schema() {
    var remoteExecutor = createRemoteExecutor();
    var schema = loadSchema(remoteExecutor);

    var subschema = Subschema.builder()
        .schema(schema)
        .executor(remoteExecutor)
        .build();

    return SchemaWrapper.wrap(subschema);
  }

  private RemoteExecutor createRemoteExecutor() {
    var endpoint = configurationProperties.getEndpoint();

    Consumer<HttpHeaders> headerBuilder = headers -> Optional.ofNullable(configurationProperties.getBearerAuth())
        .ifPresent(bearerAuth -> headers.add("Authorization", "Bearer ".concat(bearerAuth)));

    var webClient = webClientBuilder.defaultHeaders(headerBuilder)
        .build();

    return RemoteExecutor.builder()
        .endpoint(endpoint)
        .webClient(webClient)
        .build();
  }

  private GraphQLSchema loadSchema(RemoteExecutor remoteExecutor) {
    TypeDefinitionRegistry typeDefinitionRegistry;

    try {
      // TODO: custom exception
      typeDefinitionRegistry = SchemaIntrospector.introspectSchema(remoteExecutor)
          .get();
    } catch (Exception e) {
      throw internalServerErrorException(e);
    }

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, RuntimeWiring.newRuntimeWiring()
        .scalar(CoreScalars.DATE)
        .scalar(CoreScalars.DATETIME)
        .scalar(CoreScalars.OBJECT)
        .build());
  }
}
