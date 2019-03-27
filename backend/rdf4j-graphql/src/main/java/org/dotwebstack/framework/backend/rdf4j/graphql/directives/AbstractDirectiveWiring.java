package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiring;
import java.util.Optional;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
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

  NodeShape getNodeShape(@NonNull GraphQLObjectType objectType,
      @NonNull GraphQLObjectType queryType) {
    GraphQLDirective shapeDirective = Optional
        .ofNullable(objectType.getDirective(Directives.SHACL_NAME))
        .orElseThrow(() -> new InvalidConfigurationException(
            String.format("Object type '%s' requires @%s directive.", objectType.getName(),
                Directives.SHACL_NAME)));

    Rdf4jBackend shapeBackend = getBackend(
        getInheritableArgument(Directives.SHACL_ARG_BACKEND, shapeDirective, queryType));
    IRI shapeGraph = VF
        .createIRI(getInheritableArgument(Directives.SHACL_ARG_GRAPH, shapeDirective, queryType));
    IRI shapeUri = VF
        .createIRI(DirectiveUtils.getStringArgument(Directives.SHACL_ARG_SHAPE, shapeDirective));

    @Cleanup RepositoryConnection con = shapeBackend.getRepository().getConnection();
    Model shapeModel = QueryResults.asModel(con.getStatements(null, null, null, shapeGraph));

    return NodeShape.fromShapeModel(shapeModel, shapeUri);
  }

  String getInheritableArgument(@NonNull String argName, @NonNull GraphQLDirective directive,
      @NonNull GraphQLObjectType queryType) {
    String argValue = DirectiveUtils.getStringArgument(argName, directive);

    if (argValue != null) {
      return argValue;
    }

    GraphQLDirective rootDirective = queryType.getDirective(directive.getName());

    if (rootDirective != null) {
      argValue = DirectiveUtils.getStringArgument(argName, rootDirective);

      if (argValue != null) {
        return argValue;
      }
    }

    throw new InvalidConfigurationException(String
        .format("Could not find '%s' argument on @%s directive.", argName, directive.getName()));
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
