package org.dotwebstack.framework.core;

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
import org.dotwebstack.framework.core.directives.Directives;
import org.dotwebstack.framework.core.directives.SourceDirectiveWiring;
import org.dotwebstack.framework.core.scalars.Scalars;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CoreConfigurer implements Configurer {

  private final SourceDirectiveWiring sourceDirectiveWiring;

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(DirectiveDefinition.newDirectiveDefinition()
        .name(Directives.SOURCE_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SOURCE_ARG_BACKEND)
            .type(NonNullType.newNonNullType(
                TypeName.newTypeName("String").build()).build())
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
            .build())
        .build());

    registry.add(new ScalarTypeDefinition(Scalars.DATE.getName()));
    registry.add(new ScalarTypeDefinition(Scalars.DATETIME.getName()));
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder.directive(Directives.SOURCE_NAME, sourceDirectiveWiring);
    builder.scalar(Scalars.DATE);
    builder.scalar(Scalars.DATETIME);
  }

}
