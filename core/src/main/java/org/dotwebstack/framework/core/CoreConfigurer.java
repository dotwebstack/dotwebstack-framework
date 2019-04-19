package org.dotwebstack.framework.core;

import static graphql.Scalars.GraphQLString;

import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;
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

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(new ScalarTypeDefinition(CoreScalars.DATE.getName()));
    registry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));

    TypeName optionalString = TypeName.newTypeName(GraphQLString.getName()).build();
    NonNullType requiredString = NonNullType.newNonNullType(optionalString).build();

    registry.add(DirectiveDefinition.newDirectiveDefinition()
        .name(CoreDirectives.TRANSFORM_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(CoreDirectives.TRANSFORM_ARG_EXPR)
            .type(requiredString)
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
            .build())
        .build());
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder
        .scalar(CoreScalars.DATE)
        .scalar(CoreScalars.DATETIME)
        .directive(CoreDirectives.TRANSFORM_NAME, transformDirectiveWiring);
  }

}
