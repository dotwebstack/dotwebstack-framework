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
import static graphql.schema.FieldCoordinates.coordinates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.EnumTypeDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.datafetchers.DataFetcherRouter;
import org.dotwebstack.framework.core.directives.ConstraintDirectiveWiring;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.FilterDirectiveWiring;
import org.dotwebstack.framework.core.directives.TransformDirectiveWiring;
import org.dotwebstack.framework.core.input.CoreInputTypes;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
public class CoreConfigurer implements GraphqlConfigurer {

  private static final TypeName optionalString = newTypeName(GraphQLString.getName()).build();

  private static final TypeName optionalInt = newTypeName(GraphQLInt.getName()).build();

  private static final NonNullType requiredString = newNonNullType(optionalString).build();

  private static final NonNullType requiredSortEnum = NonNullType
      .newNonNullType(TypeName.newTypeName(CoreInputTypes.SORT_ORDER)
          .build())
      .build();

  private final TransformDirectiveWiring transformDirectiveWiring;

  private final ConstraintDirectiveWiring constraintDirectiveWiring;

  private final DataFetcherRouter dataFetcher;

  private TypeDefinitionRegistry typeDefinitionRegistry;

  private final FilterDirectiveWiring filterDirectiveWiring;

  public CoreConfigurer(final TransformDirectiveWiring transformDirectiveWiring,
      final ConstraintDirectiveWiring constraintDirectiveWiring, final DataFetcherRouter dataFetcher,
      FilterDirectiveWiring filterDirectiveWiring) {
    this.transformDirectiveWiring = transformDirectiveWiring;
    this.constraintDirectiveWiring = constraintDirectiveWiring;
    this.dataFetcher = dataFetcher;
    this.filterDirectiveWiring = filterDirectiveWiring;
  }

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry typeDefinitionRegistry) {
    this.typeDefinitionRegistry = typeDefinitionRegistry;
    typeDefinitionRegistry.add(new ScalarTypeDefinition(CoreScalars.DATE.getName()));
    typeDefinitionRegistry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
    typeDefinitionRegistry.add(createSortEnumDefinition());
    typeDefinitionRegistry.add(createSortInputObjectDefinition());
    typeDefinitionRegistry.add(createTransformDefinition());
    typeDefinitionRegistry.add(createConstraintDefinition());
    typeDefinitionRegistry.add(createFilterDefinition());
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
        .inputValueDefinition(newInputValueDefinition().name(CoreDirectives.TRANSFORM_ARG_EXPR)
            .type(requiredString)
            .build())
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
            newInputValueDefinition().name(CoreDirectives.CONSTRAINT_ARG_PATTERN)
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
    builder.codeRegistry(registerDataFetchers())
        .scalar(CoreScalars.DATE)
        .scalar(CoreScalars.DATETIME)
        .directive(CoreDirectives.TRANSFORM_NAME, transformDirectiveWiring)
        .directive(CoreDirectives.CONSTRAINT_NAME, constraintDirectiveWiring)
        .directive(CoreDirectives.FILTER_NAME, filterDirectiveWiring);
  }

  private GraphQLCodeRegistry registerDataFetchers() {
    GraphQLCodeRegistry.Builder builder = GraphQLCodeRegistry.newCodeRegistry();

    this.typeDefinitionRegistry.types()
        .values()
        .stream()
        .filter(type -> type instanceof ObjectTypeDefinition)
        .forEach(type -> ((ObjectTypeDefinition) type).getFieldDefinitions()
            .forEach(fieldDefinition -> builder.dataFetcher(coordinates(type.getName(), fieldDefinition.getName()),
                dataFetcher)));

    return builder.build();
  }
}
