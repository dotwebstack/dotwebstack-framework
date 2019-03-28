package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.Cleanup;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.backend.rdf4j.graphql.GraphqlObjectShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.graphql.directives.DirectiveUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.stereotype.Component;

@Component
public class ShaclDirectiveWiring extends AbstractDirectiveWiring {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final GraphqlObjectShapeRegistry objectShapeRegistry;

  ShaclDirectiveWiring(BackendRegistry backendRegistry,
      GraphqlObjectShapeRegistry objectShapeRegistry) {
    super(backendRegistry);
    this.objectShapeRegistry = objectShapeRegistry;
  }

  @Override
  public GraphQLObjectType onObject(
      SchemaDirectiveWiringEnvironment<GraphQLObjectType> environment) {
    GraphQLDirective directive = environment.getDirective();
    GraphQLObjectType objectType = environment.getElement();

    Rdf4jBackend backend = getBackend(
        DirectiveUtils.getStringArgument(Directives.SHACL_ARG_BACKEND, directive));
    IRI shapeGraph = VF.createIRI(
        DirectiveUtils.getStringArgument(Directives.SHACL_ARG_GRAPH, directive));
    IRI shape = VF
        .createIRI(DirectiveUtils.getStringArgument(Directives.SHACL_ARG_SHAPE, directive));

    @Cleanup RepositoryConnection con = backend.getRepository().getConnection();
    Model shapeModel = QueryResults.asModel(con.getStatements(null, null, null, shapeGraph));

    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, shape);
    objectShapeRegistry.register(objectType, nodeShape);

    return objectType;
  }

}
