package org.dotwebstack.framework.backend.rdf4j;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.directives.Directives;
import org.dotwebstack.framework.backend.rdf4j.model.ShapeMapping;
import org.dotwebstack.framework.backend.rdf4j.query.BindingSetFetcher;
import org.dotwebstack.framework.backend.rdf4j.query.SelectOneFetcher;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.backend.Backend;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

@RequiredArgsConstructor
final class Rdf4jBackend implements Backend {

  static final String LOCAL_BACKEND_NAME = "local";

  private static final ValueFactory vf = SimpleValueFactory.getInstance();

  private final Repository repository;

  @Override
  public DataFetcher getObjectFetcher(@NonNull GraphQLFieldDefinition fieldDefinition) {
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    if (outputType instanceof GraphQLObjectType) {
      ShapeMapping shapeMapping = getShapeMapping((GraphQLObjectType) outputType);
      return new SelectOneFetcher(repository, getShapeModel(shapeMapping.getShapeGraph()),
          shapeMapping.getShapeUri());
    }

    throw new InvalidConfigurationException(
        "Query output types other than objects are not yet supported.");
  }

  @Override
  public DataFetcher getPropertyFetcher(@NonNull GraphQLFieldDefinition fieldDefinition) {
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    if (outputType instanceof GraphQLScalarType) {
      return new BindingSetFetcher();
    }

    throw new InvalidConfigurationException(
        "Object output types other than scalars are not yet supported.");
  }

  private Model getShapeModel(IRI shapeGraph) {
    try (RepositoryConnection con = repository.getConnection()) {
      return QueryResults.asModel(con.getStatements(null, null, null, shapeGraph));
    }
  }

  private static ShapeMapping getShapeMapping(GraphQLObjectType objectType) {
    GraphQLDirective shapeDirective = Optional
        .ofNullable(objectType.getDirective(Directives.SHAPE_NAME))
        .orElseThrow(() -> new InvalidConfigurationException(
            String.format("Object type '%s' requires @%s directive.", objectType.getName(),
                Directives.SHAPE_NAME)));

    return ShapeMapping.builder()
        .shapeUri(
            vf.createIRI((String) shapeDirective.getArgument(Directives.SHAPE_ARG_URI).getValue()))
        .shapeGraph(vf.createIRI(
            (String) shapeDirective.getArgument(Directives.SHAPE_ARG_GRAPH).getValue()))
        .build();
  }

}
