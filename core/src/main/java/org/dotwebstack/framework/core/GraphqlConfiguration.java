package org.dotwebstack.framework.core;

import graphql.GraphQL;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
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
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.QueryConfiguration;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.dotwebstack.framework.core.jexl.JexlFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class GraphqlConfiguration {
  private static final String QUERY_TYPE_NAME = "Query";

  private static final String SUBSCRIPTION_TYPE_NAME = "Subscription";

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

    addQueryTypesToDefinitionRegistry(dotWebStackConfiguration, typeDefinitionRegistry);
    addObjectTypesToTypeDefinitionRegistry(dotWebStackConfiguration, typeDefinitionRegistry);

    return typeDefinitionRegistry;
  }

  private void addObjectTypesToTypeDefinitionRegistry(DotWebStackConfiguration dotWebStackConfiguration,
      TypeDefinitionRegistry typeDefinitionRegistry) {
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
  }

  private void addQueryTypesToDefinitionRegistry(DotWebStackConfiguration dotWebStackConfiguration,
      TypeDefinitionRegistry typeDefinitionRegistry) {

    var queryFieldDefinitions = dotWebStackConfiguration.getQueries()
        .entrySet()
        .stream()
        .map(entry -> createQueryFieldDefinition(entry.getKey(), entry.getValue(),
            dotWebStackConfiguration.getObjectTypes()
                .get(entry.getValue()
                    .getType())))
        .collect(Collectors.toList());

    var queryTypeDefinition = ObjectTypeDefinition.newObjectTypeDefinition()
        .name(QUERY_TYPE_NAME)
        .fieldDefinitions(
            queryFieldDefinitions.isEmpty() ? List.of(createDummyQueryFieldDefinition()) : queryFieldDefinitions)
        .build();

    typeDefinitionRegistry.add(queryTypeDefinition);
  }

  private FieldDefinition createQueryFieldDefinition(String queryName, QueryConfiguration queryConfiguration,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration) {
    return FieldDefinition.newFieldDefinition()
        .name(queryName)
        .type(createType(queryConfiguration))
        .inputValueDefinitions(queryConfiguration.getKeys()
            .stream()
            .map(keyConfiguration -> createQueryInputValueDefinition(keyConfiguration, objectTypeConfiguration))
            .collect(Collectors.toList()))
        .build();
  }

  private FieldDefinition createDummyQueryFieldDefinition() {
    return FieldDefinition.newFieldDefinition()
        .name("dummy")
        .type(TypeUtils.newType("String"))
        .build();
  }

  private InputValueDefinition createQueryInputValueDefinition(KeyConfiguration keyConfiguration,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration) {
    return InputValueDefinition.newInputValueDefinition()
        .name(keyConfiguration.getField())
        .type(createType(keyConfiguration.getField(), objectTypeConfiguration))
        .build();
  }

  private static Type<?> createType(String key,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> typeConfiguration) {
    AbstractFieldConfiguration fieldConfig = typeConfiguration.getFields()
        .get(key);
    return TypeUtils.newNonNullableType(fieldConfig.getType());
  }

  // TODO naamgeving: queryType vs QueryFieldConfiguration etc
  private static Type<?> createType(QueryConfiguration queryConfiguration) {
    var type = queryConfiguration.getType();

    if (queryConfiguration.isList()) {
      return queryConfiguration.isNullable() ? TypeUtils.newListType(type) : TypeUtils.newNonNullableListType(type);
    }

    return queryConfiguration.isNullable() ? TypeUtils.newType(type) : TypeUtils.newNonNullableType(type);
  }

  private static Type<?> createType(FieldConfiguration fieldConfiguration) {
    var type = fieldConfiguration.getType();

    if (fieldConfiguration.isList()) {
      return fieldConfiguration.isNullable() ? TypeUtils.newListType(type) : TypeUtils.newNonNullableListType(type);
    }

    return fieldConfiguration.isNullable() ? TypeUtils.newType(type) : TypeUtils.newNonNullableType(type);
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
