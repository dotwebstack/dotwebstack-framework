package org.dotwebstack.framework.core;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.language.NonNullType.newNonNullType;
import static graphql.language.TypeName.newTypeName;

import com.google.common.collect.Lists;
import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ScalarTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.TransformDirectiveWiring;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreConfigurer implements GraphqlConfigurer {

  private final TransformDirectiveWiring transformDirectiveWiring;

  private static final TypeName optionalString = newTypeName(GraphQLString.getName()).build();
  private static final TypeName optionalInt = newTypeName(GraphQLInt.getName()).build();
  private static final NonNullType requiredString = newNonNullType(optionalString).build();

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(new ScalarTypeDefinition(CoreScalars.DATE.getName()));
    registry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));

    registry.add(createTransformDefinition());
    registry.add(createConstraintDefinition());
  }

  private DirectiveDefinition createTransformDefinition() {
    return DirectiveDefinition.newDirectiveDefinition()
            .name(CoreDirectives.TRANSFORM_NAME)
            .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
                    .name(CoreDirectives.TRANSFORM_ARG_EXPR)
                    .type(requiredString)
                    .build())
            .directiveLocation(DirectiveLocation.newDirectiveLocation()
                    .name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
                    .build())
            .build();
  }

  private DirectiveDefinition createConstraintDefinition() {


    return DirectiveDefinition.newDirectiveDefinition()
            .name(CoreDirectives.CONSTRAINT_NAME)
            .inputValueDefinitions(
                    Lists.newArrayList(
                            InputValueDefinition.newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_MIN)
                                    .type(optionalInt)
                                    .build(),
                            InputValueDefinition.newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_MAX)
                                    .type(optionalInt)
                                    .build(),
                            InputValueDefinition.newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_ONEOF)
                                    .type(new ListType(new TypeName(GraphQLInt.getName())))
                                    .build()))
            .directiveLocations(Lists.newArrayList(
                    DirectiveLocation.newDirectiveLocation()
                            .name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
                            .build(),
                    DirectiveLocation.newDirectiveLocation()
                            .name(Introspection.DirectiveLocation.ARGUMENT_DEFINITION.name())
                            .build(),
                    DirectiveLocation.newDirectiveLocation()
                            .name(Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION.name())
                            .build()))
            .build();
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder
      .scalar(CoreScalars.DATE)
      .scalar(CoreScalars.DATETIME)
      .directive(CoreDirectives.TRANSFORM_NAME, transformDirectiveWiring);
  }

}
