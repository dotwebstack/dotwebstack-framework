package org.dotwebstack.framework.backend.rdf4j.graphql;

import graphql.Scalars;
import graphql.introspection.Introspection;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.graphql.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.graphql.directives.SparqlDirectiveWiring;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.graphql.GraphqlConfigurer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rdf4jConfigurer implements GraphqlConfigurer {

  private final SparqlDirectiveWiring sparqlDirectiveWiring;

  private final NodeShapeRegistry nodeShapeRegistry;

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    Type optionalString = TypeName.newTypeName(Scalars.GraphQLString.getName()).build();
    Type requiredString = NonNullType.newNonNullType(optionalString).build();

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
            .name("orderBy")
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
    builder
        .codeRegistry(registerValueFetchers())
        .directive(Rdf4jDirectives.SPARQL_NAME, sparqlDirectiveWiring);
  }

  private GraphQLCodeRegistry registerValueFetchers() {
    GraphQLCodeRegistry.Builder builder = GraphQLCodeRegistry.newCodeRegistry();

    nodeShapeRegistry.all()
        .forEach(nodeShape -> {
          String typeName = nodeShape.getIdentifier().getLocalName();

          Map<String, DataFetcher> dataFetchers = nodeShape
              .getPropertyShapes()
              .values()
              .stream()
              .collect(Collectors.toMap(PropertyShape::getName, ValueFetcher::new));

          builder.dataFetchers(typeName, dataFetchers);
        });

    return builder.build();
  }

}
