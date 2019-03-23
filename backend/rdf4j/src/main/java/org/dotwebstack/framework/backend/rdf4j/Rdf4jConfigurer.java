package org.dotwebstack.framework.backend.rdf4j;

import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.directives.Directives;
import org.dotwebstack.framework.core.Configurer;
import org.springframework.stereotype.Component;

@Component
public class Rdf4jConfigurer implements Configurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    Type stringType = NonNullType.newNonNullType(
        TypeName.newTypeName("String").build()).build();

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

    registry.add(DirectiveDefinition.newDirectiveDefinition()
        .name(Directives.SUBJECT_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SUBJECT_ARG_PREFIX)
            .type(NonNullType.newNonNullType(
                TypeName.newTypeName("String").build()).build())
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.ARGUMENT_DEFINITION.name())
            .build())
        .build());
  }

}
