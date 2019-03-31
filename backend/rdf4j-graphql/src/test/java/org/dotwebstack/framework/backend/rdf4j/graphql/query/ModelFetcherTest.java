package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import static org.dotwebstack.framework.test.Constants.BUILDING_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_EXAMPLE_2;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_2;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_PATH;
import static org.dotwebstack.framework.test.Constants.BUILDING_SHAPE;
import static org.dotwebstack.framework.test.Constants.BUILDING_SUBJECT;
import static org.dotwebstack.framework.test.Constants.BUILDING_TYPE;
import static org.dotwebstack.framework.test.Constants.SHAPE_GRAPH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import lombok.Cleanup;
import org.dotwebstack.framework.backend.rdf4j.LocalBackend;
import org.dotwebstack.framework.backend.rdf4j.LocalBackendConfigurer;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.backend.rdf4j.graphql.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.graphql.directives.Directives;
import org.dotwebstack.framework.backend.rdf4j.graphql.shacl.NodeTransformer;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.BackendConfiguration;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = BackendConfiguration.class)
@Import({LocalBackendConfigurer.class})
class ModelFetcherTest {

  @Autowired
  private BackendRegistry backendRegistry;

  @Mock
  private NodeShapeRegistry nodeShapeRegistry;

  @Mock
  private NodeTransformer nodeTransformer;

  @Mock
  private DataFetchingFieldSelectionSet selectionSet;

  @Captor
  private ArgumentCaptor<Model> modelCaptor;

  @Captor
  private ArgumentCaptor<NodeShape> nodeShapeCaptor;

  private ModelFetcher modelFetcher;

  private Model shapeModel;

  @BeforeEach
  void setUp() {
    @Cleanup RepositoryConnection con = ((Rdf4jBackend) backendRegistry
        .get(LocalBackend.LOCAL_BACKEND_NAME))
        .getRepository()
        .getConnection();

    shapeModel = QueryResults.asModel(con.getStatements(null, null, null, SHAPE_GRAPH));
    modelFetcher = new ModelFetcher(backendRegistry, nodeShapeRegistry, nodeTransformer);
  }

  @Test
  void get_ReturnsSelectedFields_ForPresentSubjectTemplate() {
    // Arrange
    List<SelectedField> selectedFields = ImmutableList
        .of(mockSelectedField(BUILDING_IDENTIFIER_FIELD, Scalars.GraphQLID));
    List<Map<String, Object>> expectedResult = ImmutableList.of(
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE_1.stringValue()));
    when(nodeTransformer
        .transform(any(Model.class), anyList(), any(NodeShape.class), eq(selectionSet)))
        .thenReturn(expectedResult);
    DataFetchingEnvironment environment = mockEnvironment(selectedFields,
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE_1.stringValue()),
        LocalBackend.LOCAL_BACKEND_NAME, BUILDING_SUBJECT);

    // Act
    Object result = modelFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(expectedResult.get(0))));
    verify(nodeTransformer)
        .transform(modelCaptor.capture(), eq(ImmutableList.of(BUILDING_EXAMPLE_1)),
            nodeShapeCaptor.capture(), eq(selectionSet));

    Model capturedModel = modelCaptor.getValue();
    assertThat(capturedModel.size(), is(equalTo(1)));
    assertThat(Models.getPropertyLiteral(capturedModel, BUILDING_EXAMPLE_1,
        BUILDING_IDENTIFIER_PATH).orElse(null), is(equalTo(BUILDING_IDENTIFIER_EXAMPLE_1)));

    NodeShape capturedNodeShape = nodeShapeCaptor.getValue();
    assertThat(capturedNodeShape.getIdentifier(), is(equalTo(BUILDING_SHAPE)));
  }

  @Test
  void get_ReturnsSelectedFields_ForAbsentSubjectTemplate() {
    // Arrange
    List<SelectedField> selectedFields = ImmutableList
        .of(mockSelectedField(BUILDING_IDENTIFIER_FIELD, Scalars.GraphQLID));
    List<Map<String, Object>> expectedResult = ImmutableList.of(
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE_1.stringValue()),
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE_2.stringValue()));
    when(nodeTransformer
        .transform(any(Model.class), anyList(), any(NodeShape.class), eq(selectionSet)))
        .thenReturn(expectedResult);
    DataFetchingEnvironment environment = mockEnvironment(
        selectedFields, ImmutableMap.of(), LocalBackend.LOCAL_BACKEND_NAME, null);

    // Act
    Object result = modelFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(expectedResult)));
    verify(nodeTransformer)
        .transform(modelCaptor.capture(),
            eq(ImmutableList.of(BUILDING_EXAMPLE_1, BUILDING_EXAMPLE_2)),
            nodeShapeCaptor.capture(), eq(selectionSet));

    Model capturedModel = modelCaptor.getValue();
    assertThat(capturedModel.size(), is(equalTo(2)));
    assertThat(Models.getPropertyLiteral(capturedModel, BUILDING_EXAMPLE_1,
        BUILDING_IDENTIFIER_PATH).orElse(null),
        is(equalTo(BUILDING_IDENTIFIER_EXAMPLE_1)));
    assertThat(Models.getPropertyLiteral(capturedModel, BUILDING_EXAMPLE_2,
        BUILDING_IDENTIFIER_PATH).orElse(null),
        is(equalTo(BUILDING_IDENTIFIER_EXAMPLE_2)));

    NodeShape capturedNodeShape = nodeShapeCaptor.getValue();
    assertThat(capturedNodeShape.getIdentifier(), is(equalTo(BUILDING_SHAPE)));
  }

  @Test
  void get_ThrowsException_ForUnknownBackend() {
    // Arrange
    DataFetchingEnvironment environment = mockEnvironment(
        ImmutableList.of(), ImmutableMap.of(), "foo", null);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        modelFetcher.get(environment));
  }

  @Test
  void get_ThrowsException_ForNonLeafType() {
    // Arrange
    List<SelectedField> selectedFields = ImmutableList
        .of(mockSelectedField(BUILDING_IDENTIFIER_FIELD, GraphQLObjectType.newObject()
            .name(BUILDING_TYPE)
            .build()));
    DataFetchingEnvironment environment = mockEnvironment(
        selectedFields, ImmutableMap.of(), LocalBackend.LOCAL_BACKEND_NAME, null);

    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        modelFetcher.get(environment));
  }

  private DataFetchingEnvironment mockEnvironment(List<SelectedField> selectedFields,
      Map<String, Object> arguments, String backend, String subject) {
    when(selectionSet.getFields()).thenReturn(selectedFields);

    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);

    GraphQLObjectType fieldType = GraphQLObjectType.newObject().name(BUILDING_TYPE).build();
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE);
    when(environment.getFieldType())
        .thenReturn(subject == null ? GraphQLList.list(fieldType) : fieldType);
    when(nodeShapeRegistry.get(fieldType)).thenReturn(nodeShape);

    when(environment.getSelectionSet()).thenReturn(selectionSet);

    GraphQLFieldDefinition fieldDefinition = mock(GraphQLFieldDefinition.class);
    when(fieldDefinition.getDirective(Directives.SPARQL_NAME))
        .thenReturn(mockSparqlDirective(backend, subject));
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);

    arguments.forEach((argKey, argValue) ->
        when(environment.getArgument(argKey)).thenReturn(argValue));
    when(environment.getArguments()).thenReturn(arguments);

    return environment;
  }

  private GraphQLDirective mockSparqlDirective(String backend, String subject) {
    GraphQLDirective.Builder builder = GraphQLDirective.newDirective()
        .name(Directives.SPARQL_NAME)
        .argument(GraphQLArgument.newArgument()
            .name(Directives.SPARQL_ARG_BACKEND)
            .type(Scalars.GraphQLString)
            .value(backend));

    if (subject != null) {
      builder.argument(GraphQLArgument.newArgument()
          .name(Directives.SPARQL_ARG_SUBJECT)
          .type(Scalars.GraphQLString)
          .value(subject));
    }

    return builder.build();
  }

  private SelectedField mockSelectedField(String name, GraphQLOutputType fieldType) {
    SelectedField field = mock(SelectedField.class);
    when(field.getName()).thenReturn(name);

    GraphQLFieldDefinition fieldDefinition = mock(GraphQLFieldDefinition.class);
    when(fieldDefinition.getType()).thenReturn(fieldType);
    when(field.getFieldDefinition()).thenReturn(fieldDefinition);

    return field;
  }

}
