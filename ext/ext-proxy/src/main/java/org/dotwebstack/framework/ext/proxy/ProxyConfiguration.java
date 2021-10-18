package org.dotwebstack.framework.ext.proxy;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.internalServerErrorException;

import graphql.GraphQL;
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

  @Bean
  @Primary
  public GraphQL graphql(RemoteExecutor remoteExecutor) {
    var schema = loadSchema(remoteExecutor);

    var subschema = Subschema.builder()
        .schema(schema)
        .executor(remoteExecutor)
        .build();

    var wrappedSchema = SchemaWrapper.wrap(subschema);

    return GraphQL.newGraphQL(wrappedSchema)
        .build();
  }

  @Bean
  public RemoteExecutor remoteExecutor(ProxyConfigurationProperties properties, WebClient.Builder webClientBuilder) {
    var endpoint = properties.getEndpoint();

    Consumer<HttpHeaders> headerBuilder = headers -> Optional.ofNullable(properties.getBearerAuth())
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
