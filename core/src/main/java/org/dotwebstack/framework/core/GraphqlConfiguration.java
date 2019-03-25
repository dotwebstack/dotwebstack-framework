package org.dotwebstack.framework.core;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.FileNotFoundException;
import java.util.Collection;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
public class GraphqlConfiguration {

  private static final String SCHEMA_PATH = "classpath:config/schema.graphqls";

  @Bean
  public GraphQLSchema graphqlSchema(@NonNull Collection<GraphqlConfigurer> graphqlConfigurers)
      throws FileNotFoundException {
    TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser()
        .parse(ResourceUtils.getFile(SCHEMA_PATH));
    graphqlConfigurers
        .forEach(graphqlConfigurer -> graphqlConfigurer
            .configureTypeDefinitionRegistry(typeDefinitionRegistry));

    RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
    graphqlConfigurers.forEach(
        graphqlConfigurer -> graphqlConfigurer.configureRuntimeWiring(runtimeWiringBuilder));

    return new SchemaGenerator()
        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  @Bean
  public GraphQL graphql(@NonNull GraphQLSchema graphqlSchema) {
    return GraphQL
        .newGraphQL(graphqlSchema)
        .build();
  }

}
