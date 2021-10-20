package org.dotwebstack.framework.core;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.CombinedWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import graphql.schema.visibility.BlockedFields;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class GraphqlConfiguration {

  @Bean
  @ConditionalOnBean(DotWebStackConfiguration.class)
  public GraphQLSchema graphqlSchema(@NonNull TypeDefinitionRegistry typeDefinitionRegistry,
      @NonNull Collection<GraphqlConfigurer> graphqlConfigurers, @NonNull List<WiringFactory> wiringFactories,
      DotWebStackConfiguration dotWebStackConfiguration) {
    var blockedFields = BlockedFields.newBlock()
        .addPatterns(createBlockPatterns(dotWebStackConfiguration))
        .build();

    var runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        .fieldVisibility(blockedFields)
        .wiringFactory(new CombinedWiringFactory(wiringFactories));

    graphqlConfigurers.forEach(graphqlConfigurer -> graphqlConfigurer.configureRuntimeWiring(runtimeWiringBuilder));

    graphqlConfigurers
        .forEach(graphqlConfigurer -> graphqlConfigurer.configureTypeDefinitionRegistry(typeDefinitionRegistry));

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  protected List<String> createBlockPatterns(DotWebStackConfiguration dotWebStackConfiguration) {
    return dotWebStackConfiguration.getObjectTypes()
        .values()
        .stream()
        .flatMap(typeConfiguration -> typeConfiguration.getFields()
            .values()
            .stream()
            .map(FieldConfiguration.class::cast)
            .filter(fieldConfiguration -> !fieldConfiguration.isVisible())
            .map(fieldConfiguration -> String.format("%s.%s", typeConfiguration.getName(),
                fieldConfiguration.getName())))
        .collect(Collectors.toList());
  }

  @Profile("!test")
  @Bean
  @ConditionalOnBean(DotWebStackConfiguration.class)
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
