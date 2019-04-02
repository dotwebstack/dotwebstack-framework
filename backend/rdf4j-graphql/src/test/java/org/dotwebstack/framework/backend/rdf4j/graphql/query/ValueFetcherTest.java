package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_EXAMPLE;
import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_PATH;
import static org.dotwebstack.framework.test.Constants.BUILDING_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_EXAMPLE_2;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_2;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_PATH;
import static org.dotwebstack.framework.test.Constants.SHAPE_GRAPH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import lombok.Cleanup;
import org.dotwebstack.framework.backend.rdf4j.LocalBackend;
import org.dotwebstack.framework.backend.rdf4j.LocalBackendConfigurer;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.backend.rdf4j.graphql.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.BackendConfiguration;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.test.Constants;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = BackendConfiguration.class)
@Import({LocalBackendConfigurer.class})
class ValueFetcherTest {

  @Autowired
  private BackendRegistry backendRegistry;

  private NodeShapeRegistry nodeShapeRegistry;

  private ValueFetcher valueFetcher;

  @BeforeEach
  void setUp() {
    nodeShapeRegistry = new NodeShapeRegistry();
    valueFetcher = new ValueFetcher(nodeShapeRegistry);
  }

  @Test
  void get_ReturnsString_ForPresentBuiltInScalarField() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(BUILDING_IDENTIFIER_FIELD)
        .type(Scalars.GraphQLID)
        .build();
    Model model = new ModelBuilder()
        .add(BUILDING_EXAMPLE_1, BUILDING_IDENTIFIER_PATH, BUILDING_IDENTIFIER_EXAMPLE_1)
        .build();
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition,
        new QuerySolution(model, BUILDING_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BUILDING_IDENTIFIER_EXAMPLE_1.stringValue())));
  }

  @Test
  void get_ReturnsString_ForPresentCustomScalarField() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(BUILDING_BUILT_AT_FIELD)
        .type(org.dotwebstack.framework.graphql.scalars.Scalars.DATETIME)
        .build();
    Model model = new ModelBuilder()
        .add(BUILDING_EXAMPLE_1, BUILDING_BUILT_AT_PATH, BUILDING_BUILT_AT_EXAMPLE)
        .build();
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition,
        new QuerySolution(model, BUILDING_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(instanceOf(Literal.class)));
  }

  @Test
  void get_ReturnsNull_ForAbsentScalarField() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(BUILDING_IDENTIFIER_FIELD)
        .type(Scalars.GraphQLID)
        .build();
    Model model = new ModelBuilder()
        .add(BUILDING_EXAMPLE_2, BUILDING_IDENTIFIER_PATH, BUILDING_IDENTIFIER_EXAMPLE_2)
        .build();
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition,
        new QuerySolution(model, BUILDING_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(nullValue()));
  }

  @Test
  void get_ThrowsException_ForNonScalarType() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(BUILDING_IDENTIFIER_FIELD)
        .type(GraphQLObjectType.newObject()
            .name(Constants.BUILDING_TYPE))
        .build();
    Model model = new ModelBuilder().build();
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition,
        new QuerySolution(model, BUILDING_EXAMPLE_1));

    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        valueFetcher.get(environment));
  }

  private DataFetchingEnvironment createEnvironment(GraphQLFieldDefinition fieldDefinition,
      QuerySolution querySolution) {
    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(environment.getFieldType()).thenReturn(fieldDefinition.getType());
    when(environment.getSource()).thenReturn(querySolution);

    GraphQLObjectType parentType = GraphQLObjectType.newObject()
        .name(Constants.BUILDING_TYPE)
        .field(fieldDefinition)
        .build();
    when(environment.getParentType()).thenReturn(parentType);

    @Cleanup RepositoryConnection con = ((Rdf4jBackend) backendRegistry
        .get(LocalBackend.LOCAL_BACKEND_NAME))
        .getRepository()
        .getConnection();

    Model shapeModel = QueryResults.asModel(con.getStatements(null, null, null, SHAPE_GRAPH));
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, Constants.BUILDING_SHAPE);
    nodeShapeRegistry.register(parentType, nodeShape);

    return environment;
  }

}
