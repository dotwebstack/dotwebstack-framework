package org.dotwebstack.framework.ext.orchestrate.config;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

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
  public GraphQLSchema graphQlSchema(Map<String, Subschema> subschemas) {
    return SchemaWrapper.wrap(subschemas.get(configurationProperties.getRoot()));
  }

  @Bean
  public Map<String, Subschema> subschemas() {
    return configurationProperties.getSubschemas()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, entry -> createSubschema(entry.getKey(), entry.getValue())));
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

    ConnectionProvider provider = ConnectionProvider.builder("orchestrate")
        .maxIdleTime(Duration.ofSeconds(10))
        .build();

    HttpClient client = HttpClient.create(provider);

    var webClient = webClientBuilder.clone()
        .defaultHeaders(headerBuilder)
        .clientConnector(new ReactorClientHttpConnector(client))
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs()
                .maxInMemorySize(5 * 1024 * 1024))
            .build())
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
