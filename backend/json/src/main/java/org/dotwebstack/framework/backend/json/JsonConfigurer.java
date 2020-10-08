package org.dotwebstack.framework.backend.json;

import static graphql.language.DirectiveLocation.newDirectiveLocation;
import static graphql.language.InputValueDefinition.newInputValueDefinition;

import com.google.common.collect.ImmutableList;
import graphql.Scalars;
import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ScalarTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.backend.json.directives.JsonDirectives;
import org.dotwebstack.framework.backend.json.directives.PredicateDirectives;
import org.dotwebstack.framework.backend.json.scalars.JsonScalars;
import org.dotwebstack.framework.core.GraphqlConfigurer;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class JsonConfigurer implements GraphqlConfigurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(createPredicateDefinition());
    registry.add(createJsonDefinition());

    Stream.of(JsonScalars.OBJECT)
        .map(GraphQLScalarType::getName)
        .map(ScalarTypeDefinition::new)
        .forEach(registry::add);
  }

  private DirectiveDefinition createPredicateDefinition() {
    TypeName optionalString = TypeName.newTypeName(Scalars.GraphQLString.getName())
        .build();

    return DirectiveDefinition.newDirectiveDefinition()
        .name(PredicateDirectives.PREDICATE_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(PredicateDirectives.ARGS_PROPERTY)
            .type(optionalString)
            .build())
        .directiveLocations(
            ImmutableList.of(newDirectiveLocation().name(Introspection.DirectiveLocation.ARGUMENT_DEFINITION.name())
                .build()))
        .build();
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

  @Override
  public void configureRuntimeWiring(@NonNull RuntimeWiring.Builder builder) {
    Stream.of(JsonScalars.OBJECT)
        .forEach(builder::scalar);
  }

}
