package org.dotwebstack.framework.backend.rdf4j;

import static graphql.language.DirectiveLocation.newDirectiveLocation;
import static graphql.language.InputValueDefinition.newInputValueDefinition;

import graphql.Scalars;
import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.NonNullType;
import graphql.language.TypeName;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.directives.SparqlDirectiveWiring;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.dotwebstack.framework.core.GraphqlConfigurer;
import org.springframework.stereotype.Component;

@Component
public class Rdf4jConfigurer implements GraphqlConfigurer {

  private final SparqlDirectiveWiring sparqlDirectiveWiring;

  public Rdf4jConfigurer(SparqlDirectiveWiring sparqlDirectiveWiring) {
    this.sparqlDirectiveWiring = sparqlDirectiveWiring;
  }

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    TypeName optionalString = TypeName.newTypeName(Scalars.GraphQLString.getName())
        .build();
    NonNullType requiredString = NonNullType.newNonNullType(optionalString)
        .build();

    registry.add(DirectiveDefinition.newDirectiveDefinition()
        .name(Rdf4jDirectives.SPARQL_NAME)
        .inputValueDefinition(newInputValueDefinition().name(Rdf4jDirectives.SPARQL_ARG_REPOSITORY)
            .type(requiredString)
            .build())
        .inputValueDefinition(newInputValueDefinition().name(Rdf4jDirectives.SPARQL_ARG_SUBJECT)
            .type(optionalString)
            .build())
        .inputValueDefinition(newInputValueDefinition().name(Rdf4jDirectives.SPARQL_ARG_DISTINCT)
            .type(TypeName.newTypeName(Scalars.GraphQLBoolean.getName())
                .build())
            .build())
        .inputValueDefinition(newInputValueDefinition().name(Rdf4jDirectives.SPARQL_ARG_LIMIT)
            .type(optionalString)
            .build())
        .inputValueDefinition(newInputValueDefinition().name(Rdf4jDirectives.SPARQL_ARG_OFFSET)
            .type(optionalString)
            .build())
        .directiveLocation(newDirectiveLocation().name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
            .build())
        .directiveLocation(newDirectiveLocation().name(Introspection.DirectiveLocation.OBJECT.name())
            .build())
        .build());
  }

  @Override
  public void configureRuntimeWiring(@NonNull RuntimeWiring.Builder builder) {
    builder.scalar(Rdf4jScalars.IRI)
        .directive(Rdf4jDirectives.SPARQL_NAME, sparqlDirectiveWiring);
  }
}
