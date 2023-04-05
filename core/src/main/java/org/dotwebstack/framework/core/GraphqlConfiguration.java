package org.dotwebstack.framework.core;

import static graphql.schema.visibility.BlockedFields.newBlock;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.createBlockedPatterns;

import graphql.GraphQL;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import graphql.schema.idl.CombinedWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
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
  public GraphQLSchema graphQlSchema(@NonNull TypeDefinitionRegistry typeDefinitionRegistry,
      @NonNull Collection<GraphqlConfigurer> graphqlConfigurers, @NonNull List<WiringFactory> wiringFactories,
      @NonNull Map<String, TypeResolver> typeResolvers) {

    var blockedFields = newBlock().addPatterns(createBlockedPatterns(typeDefinitionRegistry.types()
        .values()))
        .build();

    var runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        .fieldVisibility(blockedFields)
        .wiringFactory(new CombinedWiringFactory(wiringFactories));

    typeResolvers.forEach((interfaceName, resolver) -> {
      runtimeWiringBuilder.type(interfaceName, typeWriting -> typeWriting.typeResolver(resolver));
    });

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

  @Profile("!test")
  @Bean
  @Conditional(OnLocalSchema.class)
  public Map<String, TypeResolver> typeResolvers(TypeResolversFactory typeResolversFactory) {
    return typeResolversFactory.createTypeResolvers();
  }

  @Bean
  public GraphQL graphql(@NonNull GraphQLSchema graphqlSchema, @Nullable List<Instrumentation> instrumentations) {
    var builder = GraphQL.newGraphQL(graphqlSchema);

    if (instrumentations != null) {
      instrumentations.forEach(builder::instrumentation);
    }

    return builder.build();
  }
}
