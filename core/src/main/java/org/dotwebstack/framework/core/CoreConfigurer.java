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
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ScalarTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.core.directives.ConstraintDirectiveWiring;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.TransformDirectiveWiring;
import org.dotwebstack.framework.core.input.CoreInputTypes;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreConfigurer implements GraphqlConfigurer {

  private final TransformDirectiveWiring transformDirectiveWiring;
  private final ConstraintDirectiveWiring constraintDirectiveWiring;

  private static final TypeName optionalString = newTypeName(GraphQLString.getName()).build();
  private static final TypeName optionalInt = newTypeName(GraphQLInt.getName()).build();
  private static final NonNullType requiredString = newNonNullType(optionalString).build();
  private static final NonNullType requiredSortEnum = NonNullType.newNonNullType(
          TypeName.newTypeName(CoreInputTypes.SORT_ORDER).build()).build();

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(new ScalarTypeDefinition(CoreScalars.DATE.getName()));
    registry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
    registry.add(createSortEnumDefinition());
    registry.add(createSortInputObjectDefinition());
    registry.add(createTransformDefinition());
    registry.add(createConstraintDefinition());
  }

  private InputObjectTypeDefinition createSortInputObjectDefinition() {
    return newInputObjectDefinition()
            .name(CoreInputTypes.SORT_FIELD)
            .inputValueDefinition(newInputValueDefinition()
                    .name(CoreInputTypes.SORT_FIELD_FIELD)
                    .type(requiredString)
                    .build())
            .inputValueDefinition(newInputValueDefinition()
                    .name(CoreInputTypes.SORT_FIELD_ORDER)
                    .type(requiredSortEnum)
                    .build())
            .build();
  }

  private EnumTypeDefinition createSortEnumDefinition() {
    return newEnumTypeDefinition()
              .name(CoreInputTypes.SORT_ORDER)
              .enumValueDefinition(newEnumValueDefinition()
                      .name("ASC")
                      .build())
              .enumValueDefinition(newEnumValueDefinition()
                      .name("DESC")
                      .build())
              .build();
  }

  private DirectiveDefinition createTransformDefinition() {
    return newDirectiveDefinition()
              .name(CoreDirectives.TRANSFORM_NAME)
              .inputValueDefinition(newInputValueDefinition()
                      .name(CoreDirectives.TRANSFORM_ARG_EXPR)
                      .type(requiredString)
                      .build())
              .directiveLocation(newDirectiveLocation()
                      .name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
                      .build())
              .build();
  }

  private DirectiveDefinition createConstraintDefinition() {
    return newDirectiveDefinition()
            .name(CoreDirectives.CONSTRAINT_NAME)
            .inputValueDefinitions(
                    Lists.newArrayList(
                            newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_MIN)
                                    .type(optionalInt)
                                    .build(),
                            newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_MAX)
                                    .type(optionalInt)
                                    .build(),
                            newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_ONEOF)
                                    .type(new ListType(new TypeName(GraphQLString.getName())))
                                    .build(),
                            newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_ONEOF_INT)
                                    .type(new ListType(new TypeName(GraphQLInt.getName())))
                                    .build()))
            .directiveLocations(ImmutableList.of(
                    newDirectiveLocation()
                            .name(Introspection.DirectiveLocation.ARGUMENT_DEFINITION.name())
                            .build(),
                    newDirectiveLocation()
                            .name(Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION.name())
                            .build()))
            .build();
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder
      .scalar(CoreScalars.DATE)
      .scalar(CoreScalars.DATETIME)
      .directive(CoreDirectives.TRANSFORM_NAME, transformDirectiveWiring)
      .directive(CoreDirectives.CONSTRAINT_NAME, constraintDirectiveWiring);
  }

}
