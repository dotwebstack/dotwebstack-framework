package org.dotwebstack.framework.backend.rdf4j.graphql;

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
import org.dotwebstack.framework.backend.rdf4j.graphql.directives.Directives;
import org.dotwebstack.framework.backend.rdf4j.graphql.directives.ShaclDirectiveWiring;
import org.dotwebstack.framework.backend.rdf4j.graphql.directives.SparqlDirectiveWiring;
import org.dotwebstack.framework.graphql.GraphqlConfigurer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rdf4jGraphqlConfigurer implements GraphqlConfigurer {

  private final SparqlDirectiveWiring sparqlDirectiveWiring;

  private final ShaclDirectiveWiring shaclDirectiveWiring;

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    Type optionalString = TypeName.newTypeName("String").build();
    Type requiredString = NonNullType.newNonNullType(optionalString).build();

    registry.add(DirectiveDefinition.newDirectiveDefinition()
        .name(Directives.SPARQL_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SPARQL_ARG_BACKEND)
            .type(requiredString)
            .build())
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SPARQL_ARG_SUBJECT)
            .type(optionalString)
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.FIELD_DEFINITION.name())
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.OBJECT.name())
            .build())
        .build());

    registry.add(DirectiveDefinition.newDirectiveDefinition()
        .name(Directives.SHACL_NAME)
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SHACL_ARG_BACKEND)
            .type(requiredString)
            .build())
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SHACL_ARG_SHAPE)
            .type(requiredString)
            .build())
        .inputValueDefinition(InputValueDefinition.newInputValueDefinition()
            .name(Directives.SHACL_ARG_GRAPH)
            .type(requiredString)
            .build())
        .directiveLocation(DirectiveLocation.newDirectiveLocation()
            .name(Introspection.DirectiveLocation.OBJECT.name())
            .build())
        .build());
  }

  @Override
  public void configureRuntimeWiring(@NonNull RuntimeWiring.Builder builder) {
    builder.directive(Directives.SPARQL_NAME, sparqlDirectiveWiring);
    builder.directive(Directives.SHACL_NAME, shaclDirectiveWiring);
  }

}
