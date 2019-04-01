package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Cleanup;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.LocalBackend;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.backend.rdf4j.graphql.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.ValueFetcher;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.Backend;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShaclDirectiveWiring implements SchemaDirectiveWiring {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final BackendRegistry backendRegistry;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final ValueFetcher valueFetcher;

  private final IRI shapeGraph;

  private Model shapeModel;

  ShaclDirectiveWiring(@NonNull BackendRegistry backendRegistry,
      @NonNull NodeShapeRegistry nodeShapeRegistry, @NonNull ValueFetcher valueFetcher,
      @Value("${dotwebstack.rdf4j.shapeGraph}") String shapeGraph) {
    this.backendRegistry = backendRegistry;
    this.nodeShapeRegistry = nodeShapeRegistry;
    this.valueFetcher = valueFetcher;
    this.shapeGraph = VF.createIRI(shapeGraph);
  }

  @PostConstruct
  void initialize() {
    @Cleanup RepositoryConnection con = getBackend(LocalBackend.LOCAL_BACKEND_NAME)
        .getRepository()
        .getConnection();
    shapeModel = QueryResults.asModel(con.getStatements(null, null, null, shapeGraph));
  }

  @Override
  public GraphQLObjectType onObject(
      @NonNull SchemaDirectiveWiringEnvironment<GraphQLObjectType> environment) {
    GraphQLObjectType objectType = environment.getElement();

    Resource shape = (Resource) environment.getDirective()
        .getArgument(Directives.SHACL_ARG_SHAPE)
        .getValue();

    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, shape);
    nodeShapeRegistry.register(objectType, nodeShape);

    environment.getCodeRegistry()
        .dataFetchers(objectType.getName(), objectType.getFieldDefinitions()
            .stream()
            .map(GraphQLFieldDefinition::getName)
            .collect(Collectors.toMap(Function.identity(), n -> valueFetcher)));

    return objectType;
  }

  private Rdf4jBackend getBackend(String backendName) {
    Backend backend = backendRegistry.get(backendName);

    if (!(backend instanceof Rdf4jBackend)) {
      throw new InvalidConfigurationException(
          String.format("Backend '%s' not found or is not an RDF4J backend.", backendName));
    }

    return (Rdf4jBackend) backend;
  }

}
