package org.dotwebstack.framework.ext.orchestrate.config;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.dotwebstack.framework.ext.orchestrate.SubschemaModifier;
import org.dotwebstack.framework.ext.orchestrate.config.OrchestrateConfigurationProperties.SubschemaProperties;
import org.dotwebstack.graphql.orchestrate.schema.RemoteExecutor;
import org.dotwebstack.graphql.orchestrate.schema.SchemaIntrospector;
import org.dotwebstack.graphql.orchestrate.schema.Subschema;
import org.dotwebstack.graphql.orchestrate.transform.TransformUtils;
import org.dotwebstack.graphql.orchestrate.wrap.SchemaWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class OrchestrateConfiguration {

  private final OrchestrateConfigurationProperties configurationProperties;

  private final WebClient.Builder webClientBuilder;

  private final Collection<SubschemaModifier> subschemaModifiers;

  public OrchestrateConfiguration(OrchestrateConfigurationProperties configurationProperties,
      WebClient.Builder webClientBuilder, Collection<SubschemaModifier> subschemaModifiers) {
    this.configurationProperties = configurationProperties;
    this.webClientBuilder = webClientBuilder;
    this.subschemaModifiers = subschemaModifiers;
  }

  @Bean
  @Primary
  public GraphQLSchema schema() {
    var rootSubschemaProperties = configurationProperties.getSubschemas()
        .get(configurationProperties.getRoot());

    return SchemaWrapper.wrap(createSubschema(configurationProperties.getRoot(), rootSubschemaProperties));
  }

  private Subschema createSubschema(String key, SubschemaProperties subschemaProperties) {
    var remoteExecutor = createRemoteExecutor(subschemaProperties);
    var schema = loadSchema(remoteExecutor);

    var subschema = Subschema.newSubschema()
        .schema(schema)
        .executor(remoteExecutor)
        .build();

    return subschemaModifiers.stream()
        .reduce(subschema, (acc, modifier) -> modifier.modify(key, subschema), TransformUtils::noopCombiner);
  }

  private RemoteExecutor createRemoteExecutor(SubschemaProperties subschemaProperties) {
    var endpoint = subschemaProperties.getEndpoint();

    Consumer<HttpHeaders> headerBuilder = headers -> Optional.ofNullable(subschemaProperties.getBearerAuth())
        .ifPresent(bearerAuth -> headers.add("Authorization", "Bearer ".concat(bearerAuth)));

    var webClient = webClientBuilder.defaultHeaders(headerBuilder)
        .build();

    return RemoteExecutor.newExecutor()
        .endpoint(endpoint)
        .webClient(webClient)
        .build();
  }

  @SneakyThrows
  private GraphQLSchema loadSchema(RemoteExecutor remoteExecutor) {
    TypeDefinitionRegistry typeDefinitionRegistry = SchemaIntrospector.introspectSchema(remoteExecutor)
        .get();

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, RuntimeWiring.newRuntimeWiring()
        .scalar(CoreScalars.DATE)
        .scalar(CoreScalars.DATETIME)
        .scalar(CoreScalars.OBJECT)
        .build());
  }
}
