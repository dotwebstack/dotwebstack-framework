package org.dotwebstack.framework.backend.rdf4j;

import static graphql.language.DirectiveLocation.newDirectiveLocation;

import com.google.common.collect.ImmutableList;
import graphql.Scalars;
import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.TypeName;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.directives.DirectiveWiring;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.dotwebstack.framework.core.GraphqlConfigurer;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.FilterDirectiveWiring;
import org.springframework.stereotype.Component;

@Component
public class Rdf4jConfigurer implements GraphqlConfigurer {

  private final DirectiveWiring directiveWiring;

  public Rdf4jConfigurer(DirectiveWiring directiveWiring) {
    this.directiveWiring = directiveWiring;
  }

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    TypeName optionalString = TypeName.newTypeName(Scalars.GraphQLString.getName())
        .build();
    NonNullType requiredString = NonNullType.newNonNullType(optionalString)
        .build();

    registry.add(DirectiveDefinition.newDirectiveDefinition()
        .name(Rdf4jDirectives.SPARQL_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Rdf4jDirectives.SPARQL_ARG_REPOSITORY)
            .type(requiredString)
            .build())
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Rdf4jDirectives.SPARQL_ARG_SUBJECT)
            .type(optionalString)
            .build())
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Rdf4jDirectives.SPARQL_ARG_LIMIT)
            .type(optionalString)
            .build())
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Rdf4jDirectives.SPARQL_ARG_OFFSET)
            .type(optionalString)
            .build())
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Rdf4jDirectives.SPARQL_ARG_ORDER_BY)
            .type(optionalString)
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.OBJECT.name())
            .build())
        .build());
  }

  @Override
  public void configureRuntimeWiring(@NonNull RuntimeWiring.Builder builder) {
    builder.scalar(Rdf4jScalars.IRI)
        .directive(Rdf4jDirectives.SPARQL_NAME, directiveWiring);
  }
}
