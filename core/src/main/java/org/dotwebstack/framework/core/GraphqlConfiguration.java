package org.dotwebstack.framework.core;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.CombinedWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import graphql.schema.visibility.BlockedFields;
import graphql.schema.visibility.GraphqlFieldVisibility;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Conditional(GraphQlNativeEnabled.class)
@Slf4j
@Configuration
public class GraphqlConfiguration {

  @Bean
  public GraphQLSchema graphqlSchema(@NonNull TypeDefinitionRegistry typeDefinitionRegistry,
      @NonNull Collection<GraphqlConfigurer> graphqlConfigurers, @NonNull List<WiringFactory> wiringFactories,
      DotWebStackConfiguration dotWebStackConfiguration) {

    var runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        .fieldVisibility(createFieldVisibility(dotWebStackConfiguration))
        .wiringFactory(new CombinedWiringFactory(wiringFactories));

    graphqlConfigurers.forEach(graphqlConfigurer -> graphqlConfigurer.configureRuntimeWiring(runtimeWiringBuilder));

    graphqlConfigurers
        .forEach(graphqlConfigurer -> graphqlConfigurer.configureTypeDefinitionRegistry(typeDefinitionRegistry));

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  private GraphqlFieldVisibility createFieldVisibility(DotWebStackConfiguration dotWebStackConfiguration) {
    var blockedPatterns = dotWebStackConfiguration.getObjectTypes()
        .values()
        .stream()
        .flatMap(typeConfiguration -> typeConfiguration.getFields()
            .values()
            .stream()
            .map(o -> (FieldConfiguration) o)
            .filter(fieldConfiguration -> !fieldConfiguration.isVisible())
            .map(fieldConfiguration -> String.format("%s.%s", typeConfiguration.getName(),
                fieldConfiguration.getName())))
        .collect(Collectors.toList());

    return BlockedFields.newBlock()
        .addPatterns(blockedPatterns)
        .build();
  }

  @Profile("!test")
  @Bean
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
