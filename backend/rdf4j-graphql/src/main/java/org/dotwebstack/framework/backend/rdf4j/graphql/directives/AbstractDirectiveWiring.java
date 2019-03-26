package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiring;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.backend.rdf4j.graphql.model.NodeShape;
import org.dotwebstack.framework.core.Backend;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.graphql.directives.DirectiveUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;

@RequiredArgsConstructor
abstract class AbstractDirectiveWiring implements SchemaDirectiveWiring {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final BackendRegistry backendRegistry;

  NodeShape getNodeShape(GraphQLObjectType objectType) {
    GraphQLDirective shapeDirective = Optional
        .ofNullable(objectType.getDirective(Directives.SHAPE_NAME))
        .orElseThrow(() -> new InvalidConfigurationException(
            String.format("Object type '%s' requires @%s directive.", objectType.getName(),
                Directives.SHAPE_NAME)));

    Rdf4jBackend shapeBackend = getBackend(
        DirectiveUtils.getStringArgument(shapeDirective, Directives.SHAPE_ARG_BACKEND));
    IRI shapeGraph = VF
        .createIRI(DirectiveUtils.getStringArgument(shapeDirective, Directives.SHAPE_ARG_GRAPH));
    IRI shapeUri = VF
        .createIRI(DirectiveUtils.getStringArgument(shapeDirective, Directives.SHAPE_ARG_URI));

    try (RepositoryConnection con = shapeBackend.getRepository().getConnection()) {
      Model shapeModel = QueryResults.asModel(con.getStatements(null, null, null, shapeGraph));
      return NodeShape.fromShapeModel(shapeModel, shapeUri);
    }
  }

  Rdf4jBackend getBackend(String backendName) {
    Backend backend = backendRegistry.get(backendName);

    if (!(backend instanceof Rdf4jBackend)) {
      throw new InvalidConfigurationException(
          String.format("Backend '%s' not found or is not an RDF4J backend.", backendName));
    }

    return (Rdf4jBackend) backend;
  }

}
