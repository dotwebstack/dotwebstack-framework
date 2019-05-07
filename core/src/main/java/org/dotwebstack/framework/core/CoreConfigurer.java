package org.dotwebstack.framework.core;

import static graphql.Scalars.GraphQLString;
import static graphql.language.NonNullType.newNonNullType;
import static graphql.language.TypeName.newTypeName;

import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.ScalarTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.core.directives.ConstraintDirectiveWiring;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.TransformDirectiveWiring;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreConfigurer implements GraphqlConfigurer {

  private final TransformDirectiveWiring transformDirectiveWiring;
  private final ConstraintDirectiveWiring constraintDirectiveWiring;

  private static final TypeName optionalString = newTypeName(GraphQLString.getName()).build();
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
                    List.of(
                            InputValueDefinition.newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_MIN)
                                    .type(optionalString)
                                    .build(),
                            InputValueDefinition.newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_MAX)
                                    .type(optionalString)
                                    .build(),
                            InputValueDefinition.newInputValueDefinition()
                                    .name(CoreDirectives.CONSTRAINT_ARG_ONEOF)
                                    .type(optionalString)
                                    .build()))
            .directiveLocation(
                    DirectiveLocation.newDirectiveLocation()
                            .name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
                            .build())
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
