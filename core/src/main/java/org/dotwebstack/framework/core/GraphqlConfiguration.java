package org.dotwebstack.framework.core;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.CombinedWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class GraphqlConfiguration {

  @Bean
  @Conditional(OnLocalSchema.class)
  public GraphQLSchema graphqlSchema(@NonNull TypeDefinitionRegistry typeDefinitionRegistry,
      @NonNull Collection<GraphqlConfigurer> graphqlConfigurers, @NonNull List<WiringFactory> wiringFactories) {
    var runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        .wiringFactory(new CombinedWiringFactory(wiringFactories));

    graphqlConfigurers.forEach(graphqlConfigurer -> graphqlConfigurer.configureRuntimeWiring(runtimeWiringBuilder));

    graphqlConfigurers
        .forEach(graphqlConfigurer -> graphqlConfigurer.configureTypeDefinitionRegistry(typeDefinitionRegistry));

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  @Profile("!test")
  @Bean
  @Conditional(OnLocalSchema.class)
  public TypeDefinitionRegistry typeDefinitionRegistry(
      TypeDefinitionRegistrySchemaFactory typeDefinitionRegistryFactory) {
    return typeDefinitionRegistryFactory.createTypeDefinitionRegistry();
  }

  @Bean
  public GraphQL graphql(@NonNull GraphQLSchema graphqlSchema) {
    return GraphQL.newGraphQL(graphqlSchema)
        .build();
  }
}
