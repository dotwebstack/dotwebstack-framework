package org.dotwebstack.framework.backend.rdf4j;

import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.directives.Directives;
import org.dotwebstack.framework.backend.rdf4j.directives.SelectDirectiveWiring;
import org.dotwebstack.framework.core.Configurer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rdf4jConfigurer implements Configurer {

  private final SelectDirectiveWiring selectDirectiveWiring;

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    Type stringType = NonNullType.newNonNullType(
        TypeName.newTypeName("String").build()).build();

    registry.add(DirectiveDefinition.newDirectiveDefinition()
        .name(Directives.SELECT_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SELECT_ARG_SUBJECT)
            .type(TypeName.newTypeName("String").build())
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
            .build())
        .build());

    registry.add(DirectiveDefinition.newDirectiveDefinition()
        .name(Directives.SHAPE_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SHAPE_ARG_URI)
            .type(stringType)
            .build())
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SHAPE_ARG_GRAPH)
            .type(stringType)
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.OBJECT.name())
            .build())
        .build());
  }

  @Override
  public void configureRuntimeWiring(@NonNull RuntimeWiring.Builder builder) {
    builder.directive(Directives.SELECT_NAME, selectDirectiveWiring);
  }

}
