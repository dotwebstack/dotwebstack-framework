package org.dotwebstack.framework.core;

import graphql.GraphQL;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.CombinedWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.dotwebstack.framework.core.jexl.JexlFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class GraphqlConfiguration {

  @Bean
  public GraphQLSchema graphqlSchema(@NonNull TypeDefinitionRegistry typeDefinitionRegistry,
      @NonNull Collection<GraphqlConfigurer> graphqlConfigurers, @NonNull List<SchemaValidator> schemaValidators,
      @NonNull List<WiringFactory> wiringFactories) {
    var runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        .wiringFactory(new CombinedWiringFactory(wiringFactories));

    graphqlConfigurers.forEach(graphqlConfigurer -> graphqlConfigurer.configureRuntimeWiring(runtimeWiringBuilder));

    graphqlConfigurers
        .forEach(graphqlConfigurer -> graphqlConfigurer.configureTypeDefinitionRegistry(typeDefinitionRegistry));

    schemaValidators.forEach(SchemaValidator::validate);

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  @Profile("!test")
  @Bean
  public TypeDefinitionRegistry typeDefinitionRegistry(DotWebStackConfiguration dotWebStackConfiguration) {
    var typeDefinitionRegistry = new TypeDefinitionRegistry();

    typeDefinitionRegistry.add(ObjectTypeDefinition.newObjectTypeDefinition()
        .name("Query")
        .fieldDefinition(FieldDefinition.newFieldDefinition()
            .name("ping")
            .type(TypeUtils.newNonNullableListType("Beer"))
            .build())
        .build());

    dotWebStackConfiguration.getObjectTypes()
        .forEach((name, objectType) -> {
          var objectTypeDefinition = ObjectTypeDefinition.newObjectTypeDefinition()
              .name(name)
              .fieldDefinitions(objectType.getFields()
                  .entrySet()
                  .stream()
                  .map(entry -> FieldDefinition.newFieldDefinition()
                      .name(entry.getKey())
                      .type(createType(entry.getValue()))
                      .build())
                  .collect(Collectors.toList()))
              .build();

          objectType.init(dotWebStackConfiguration.getObjectTypes(), objectTypeDefinition);
          typeDefinitionRegistry.add(objectTypeDefinition);
        });

    return typeDefinitionRegistry;
  }

  private static Type<?> createType(FieldConfiguration fieldConfiguration) {
    var name = fieldConfiguration.getType();

    if (fieldConfiguration.isList()) {
      return fieldConfiguration.isNullable() ? TypeUtils.newListType(name) : TypeUtils.newNonNullableListType(name);
    }

    return fieldConfiguration.isNullable() ? TypeUtils.newType(name) : TypeUtils.newNonNullableType(name);
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
