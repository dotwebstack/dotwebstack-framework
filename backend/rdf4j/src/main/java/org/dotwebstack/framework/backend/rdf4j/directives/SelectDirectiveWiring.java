package org.dotwebstack.framework.backend.rdf4j.directives;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.model.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.model.ShapeReference;
import org.dotwebstack.framework.backend.rdf4j.query.BindingSetFetcher;
import org.dotwebstack.framework.backend.rdf4j.query.SelectOneFetcher;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SelectDirectiveWiring implements SchemaDirectiveWiring {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final RepositoryConnection repositoryConnection;

  @Override
  public GraphQLFieldDefinition onField(
      @NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLFieldsContainer parentType = environment.getFieldsContainer();

    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    if (!(outputType instanceof GraphQLObjectType)) {
      throw new InvalidConfigurationException(
          "Query output types other than objects are not yet supported.");
    }

    GraphQLObjectType objectType = (GraphQLObjectType) outputType;

    ShapeReference shapeReference = getShapeReference(objectType);
    Model shapeModel = getShapeModel(shapeReference.getGraph());
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, shapeReference.getUri());

    String subjectTemplate = (String) environment.getDirective()
        .getArgument(Directives.SELECT_ARG_SUBJECT).getValue();
    DataFetcher objectFetcher = new SelectOneFetcher(repositoryConnection, nodeShape,
        subjectTemplate);
    environment.getCodeRegistry().dataFetcher(parentType, fieldDefinition, objectFetcher);

    DataFetcher scalarFetcher = new BindingSetFetcher();
    objectType.getFieldDefinitions()
        .stream()
        .filter(childFieldDefinition -> GraphQLTypeUtil
            .unwrapNonNull(childFieldDefinition.getType()) instanceof GraphQLScalarType)
        .forEach(childFieldDefinition -> environment.getCodeRegistry()
            .dataFetcher(objectType, childFieldDefinition, scalarFetcher));

    return fieldDefinition;
  }

  private Model getShapeModel(IRI shapeGraph) {
    return QueryResults.asModel(repositoryConnection.getStatements(null, null, null, shapeGraph));
  }

  private static ShapeReference getShapeReference(GraphQLObjectType objectType) {
    GraphQLDirective shapeDirective = Optional
        .ofNullable(objectType.getDirective(Directives.SHAPE_NAME))
        .orElseThrow(() -> new InvalidConfigurationException(
            String.format("Object type '%s' requires @%s directive.", objectType.getName(),
                Directives.SHAPE_NAME)));

    return ShapeReference.builder()
        .uri(VF.createIRI((String) shapeDirective.getArgument(Directives.SHAPE_ARG_URI).getValue()))
        .graph(VF.createIRI(
            (String) shapeDirective.getArgument(Directives.SHAPE_ARG_GRAPH).getValue()))
        .build();
  }

}
