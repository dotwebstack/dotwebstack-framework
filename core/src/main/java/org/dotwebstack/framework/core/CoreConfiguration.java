package org.dotwebstack.framework.core;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.FileNotFoundException;
import java.util.Collection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
public class CoreConfiguration {

  private static final String SCHEMA_PATH = "classpath:config/schema.graphqls";

  @Bean
  public GraphQLSchema graphqlSchema(Collection<Configurer> configurers)
      throws FileNotFoundException {
    TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser()
        .parse(ResourceUtils.getFile(SCHEMA_PATH));
    configurers
        .forEach(configurer -> configurer.configureTypeDefinitionRegistry(typeDefinitionRegistry));

    RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
    configurers.forEach(configurer -> configurer.configureRuntimeWiring(runtimeWiringBuilder));

    return new SchemaGenerator()
        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  @Bean
  public GraphQL graphql(GraphQLSchema graphqlSchema) {
    return GraphQL
        .newGraphQL(graphqlSchema)
        .build();
  }

}
