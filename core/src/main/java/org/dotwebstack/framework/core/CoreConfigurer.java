package org.dotwebstack.framework.core;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.language.DirectiveDefinition.newDirectiveDefinition;
import static graphql.language.DirectiveLocation.newDirectiveLocation;
import static graphql.language.EnumTypeDefinition.newEnumTypeDefinition;
import static graphql.language.EnumValueDefinition.newEnumValueDefinition;
import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.NonNullType.newNonNullType;
import static graphql.language.TypeName.newTypeName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.EnumTypeDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ScalarTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.core.datafetchers.DataFetcherRouter;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.input.CoreInputTypes;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
public class CoreConfigurer implements GraphqlConfigurer {

  private static final TypeName optionalString = newTypeName(GraphQLString.getName()).build();

  private static final TypeName optionalInt = newTypeName(GraphQLInt.getName()).build();

  private static final NonNullType requiredString = newNonNullType(optionalString).build();

  private static final TypeName optionalSortEnum = TypeName.newTypeName(CoreInputTypes.SORT_ORDER)
      .build();

  private static final NonNullType requiredSortEnum = NonNullType.newNonNullType(optionalSortEnum)
      .build();

  private final DataFetcherRouter dataFetcher;

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  private final List<AutoRegisteredSchemaDirectiveWiring> autoRegisteredSchemaDirectiveWirings;

  public CoreConfigurer(final DataFetcherRouter dataFetcher,
      final List<AutoRegisteredSchemaDirectiveWiring> autoRegisteredSchemaDirectiveWirings,
      TypeDefinitionRegistry typeDefinitionRegistry) {
    this.dataFetcher = dataFetcher;
    this.autoRegisteredSchemaDirectiveWirings = autoRegisteredSchemaDirectiveWirings;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    typeDefinitionRegistry.add(new ScalarTypeDefinition(CoreScalars.OBJECT.getName()));
    typeDefinitionRegistry.add(new ScalarTypeDefinition(CoreScalars.DATE.getName()));
    typeDefinitionRegistry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
    typeDefinitionRegistry.add(createSortEnumDefinition());
    typeDefinitionRegistry.add(createSortInputObjectDefinition());
    typeDefinitionRegistry.add(createTransformDefinition());
    typeDefinitionRegistry.add(createConstraintDefinition());
    typeDefinitionRegistry.add(createFilterDefinition());
    typeDefinitionRegistry.add(createSortDefinition());
    typeDefinitionRegistry.add(createOffsetDefinition());
    typeDefinitionRegistry.add(createLimitDefinition());
  }

  private DirectiveDefinition createSortDefinition() {
    return newDirectiveDefinition().name(CoreDirectives.SORT_NAME)
        .directiveLocations(ImmutableList.of(
            newDirectiveLocation().name(Introspection.DirectiveLocation.ARGUMENT_DEFINITION.name())
                .build(),
            newDirectiveLocation().name(Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION.name())
                .build()))
        .build();
  }

  private DirectiveDefinition createLimitDefinition() {
    return newDirectiveDefinition().name(CoreDirectives.LIMIT_NAME)
        .inputValueDefinition(newInputValueDefinition().name(CoreDirectives.LIMIT_EXPR)
            .type(optionalString)
            .build())
        .directiveLocation(newDirectiveLocation().name(Introspection.DirectiveLocation.ARGUMENT_DEFINITION.name())
            .build())
        .build();
  }

  private DirectiveDefinition createOffsetDefinition() {
    return newDirectiveDefinition().name(CoreDirectives.OFFSET_NAME)
        .inputValueDefinition(newInputValueDefinition().name(CoreDirectives.OFFSET_EXPR)
            .type(optionalString)
            .build())
        .directiveLocation(newDirectiveLocation().name(Introspection.DirectiveLocation.ARGUMENT_DEFINITION.name())
            .build())
        .build();
  }

  private DirectiveDefinition createFilterDefinition() {
    return DirectiveDefinition.newDirectiveDefinition()
        .name(CoreDirectives.FILTER_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(CoreDirectives.FILTER_ARG_FIELD)
            .type(optionalString)
            .build())
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(CoreDirectives.FILTER_ARG_OPERATOR)
            .type(optionalString)
            .build())
        .directiveLocations(ImmutableList.of(
            newDirectiveLocation().name(Introspection.DirectiveLocation.ARGUMENT_DEFINITION.name())
                .build(),
            newDirectiveLocation().name(Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION.name())
                .build()))
        .build();
  }

  private InputObjectTypeDefinition createSortInputObjectDefinition() {
    return newInputObjectDefinition().name(CoreInputTypes.SORT_FIELD)
        .inputValueDefinition(newInputValueDefinition().name(CoreInputTypes.SORT_FIELD_FIELD)
            .type(requiredString)
            .build())
        .inputValueDefinition(newInputValueDefinition().name(CoreInputTypes.SORT_FIELD_ORDER)
            .type(requiredSortEnum)
            .build())
        .build();
  }

  private EnumTypeDefinition createSortEnumDefinition() {
    return newEnumTypeDefinition().name(CoreInputTypes.SORT_ORDER)
        .enumValueDefinition(newEnumValueDefinition().name("ASC")
            .build())
        .enumValueDefinition(newEnumValueDefinition().name("DESC")
            .build())
        .build();
  }

  private DirectiveDefinition createTransformDefinition() {
    return newDirectiveDefinition().name(CoreDirectives.TRANSFORM_NAME)
        .inputValueDefinitions(Lists.newArrayList(newInputValueDefinition().name(CoreDirectives.TRANSFORM_ARG_EXPR)
            .type(requiredString)
            .build(),
            newInputValueDefinition().name(CoreDirectives.TRANSFORM_ARG_TYPE)
                .type(optionalString)
                .build()))
        .directiveLocation(newDirectiveLocation().name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
            .build())
        .build();
  }

  private DirectiveDefinition createConstraintDefinition() {
    return newDirectiveDefinition().name(CoreDirectives.CONSTRAINT_NAME)
        .inputValueDefinitions(Lists.newArrayList(newInputValueDefinition().name(CoreDirectives.CONSTRAINT_ARG_MIN)
            .type(optionalInt)
            .build(),
            newInputValueDefinition().name(CoreDirectives.CONSTRAINT_ARG_MAX)
                .type(optionalInt)
                .build(),
            newInputValueDefinition().name(CoreDirectives.CONSTRAINT_ARG_ONEOF)
                .type(new ListType(new TypeName(GraphQLString.getName())))
                .build(),
            newInputValueDefinition().name(CoreDirectives.CONSTRAINT_ARG_ONEOF_INT)
                .type(new ListType(new TypeName(GraphQLInt.getName())))
                .build(),
            newInputValueDefinition().name(CoreDirectives.CONSTRAINT_ARG_VALUESIN)
                .type(new ListType(new TypeName(GraphQLString.getName())))
                .build(),
            newInputValueDefinition().name(CoreDirectives.CONSTRAINT_ARG_PATTERN)
                .type(new TypeName(GraphQLString.getName()))
                .build(),
            newInputValueDefinition().name(CoreDirectives.CONSTRAINT_ARG_EXPR)
                .type(new TypeName(GraphQLString.getName()))
                .build()))
        .directiveLocations(ImmutableList.of(
            newDirectiveLocation().name(Introspection.DirectiveLocation.ARGUMENT_DEFINITION.name())
                .build(),
            newDirectiveLocation().name(Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION.name())
                .build()))
        .build();
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder.scalar(CoreScalars.OBJECT)
        .scalar(CoreScalars.DATE)
        .scalar(CoreScalars.DATETIME);

//    autoRegisteredSchemaDirectiveWirings.forEach(wiring -> builder.directive(wiring.getDirectiveName(), wiring));
  }
}
