package org.dotwebstack.framework.core;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.CombinedWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.dotwebstack.framework.core.jexl.JexlFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Slf4j
@Configuration
public class GraphqlConfiguration {

  private static final String SCHEMA_FILE = "schema.graphqls";

  @Bean
  public GraphQLSchema graphqlSchema(@NonNull TypeDefinitionRegistry typeDefinitionRegistry,
      @NonNull Collection<GraphqlConfigurer> graphqlConfigurers, @NonNull List<SchemaValidator> schemaValidators,
      @NonNull List<WiringFactory> wiringFactories) {
    RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        .wiringFactory(new CombinedWiringFactory(wiringFactories));

    graphqlConfigurers.forEach(graphqlConfigurer -> graphqlConfigurer.configureRuntimeWiring(runtimeWiringBuilder));

    graphqlConfigurers
        .forEach(graphqlConfigurer -> graphqlConfigurer.configureTypeDefinitionRegistry(typeDefinitionRegistry));

    schemaValidators.forEach(SchemaValidator::validate);

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  @Profile("!test")
  @Bean
  public TypeDefinitionRegistry typeDefinitionRegistry() throws IOException {
    Optional<Resource> schemaLocationResource = ResourceLoaderUtils.getResource(SCHEMA_FILE);
    if (schemaLocationResource.isEmpty() || !schemaLocationResource.get()
        .exists()) {
      throw invalidConfigurationException("Graphql schema not found on location: {}", SCHEMA_FILE);
    }
    Reader reader = new InputStreamReader(schemaLocationResource.get()
        .getInputStream());

    return new SchemaParser().parse(reader);
  }

  @Bean
  public GraphQL graphql(@NonNull GraphQLSchema graphqlSchema) {
    return GraphQL.newGraphQL(graphqlSchema)
        .build();
  }

  @Bean
  public JexlEngine jexlBuilder(List<JexlFunction> jexlFunctions) {
    Map<String, Object> namespaces = jexlFunctions.stream()
        .collect(Collectors.toMap(JexlFunction::getNamespace, function -> function));
    LOG.debug("Loading JEXL functions [{}]", namespaces);
    return new JexlBuilder().silent(false)
        .namespaces(namespaces)
        .strict(true)
        .create();
  }
}
