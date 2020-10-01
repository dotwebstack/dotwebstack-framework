package org.dotwebstack.framework.backend.json;

import static graphql.language.DirectiveLocation.newDirectiveLocation;
import static graphql.language.InputValueDefinition.newInputValueDefinition;

import graphql.Scalars;
import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.backend.json.directives.JsonDirectives;
import org.dotwebstack.framework.core.GraphqlConfigurer;
import org.springframework.stereotype.Component;

@Component
public class JsonConfigurer implements GraphqlConfigurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(createJsonDefinition());
  }

  private DirectiveDefinition createJsonDefinition() {
    TypeName optionalString = TypeName.newTypeName(Scalars.GraphQLString.getName())
        .build();
    NonNullType requiredString = NonNullType.newNonNullType(optionalString)
        .build();

    ListType predicateType = ListType.newListType(requiredString)
        .build();

    return DirectiveDefinition.newDirectiveDefinition()
        .name(JsonDirectives.JSON_NAME)
        .inputValueDefinition(newInputValueDefinition().name(JsonDirectives.ARGS_FILE)
            .type(requiredString)
            .build())
        .inputValueDefinition(newInputValueDefinition().name(JsonDirectives.ARGS_PATH)
            .type(requiredString)
            .build())
        .inputValueDefinition(newInputValueDefinition().name(JsonDirectives.ARGS_PREDICATES)
            .type(predicateType)
            .build())

        .directiveLocation(newDirectiveLocation().name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
            .build())
        .directiveLocation(newDirectiveLocation().name(Introspection.DirectiveLocation.OBJECT.name())
            .build())
        .build();
  }

}
