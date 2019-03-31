package org.dotwebstack.framework.backend.rdf4j.graphql.shacl;

import static org.dotwebstack.framework.test.Constants.BUILDING_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_EXAMPLE_2;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_2;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_PATH;
import static org.dotwebstack.framework.test.Constants.BUILDING_SHAPE;
import static org.dotwebstack.framework.test.Constants.SHAPE_GRAPH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.Scalars;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Cleanup;
import org.dotwebstack.framework.backend.rdf4j.LocalBackend;
import org.dotwebstack.framework.backend.rdf4j.LocalBackendConfigurer;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.BackendConfiguration;
import org.dotwebstack.framework.core.BackendRegistry;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = BackendConfiguration.class)
@Import({LocalBackendConfigurer.class})
class NodeTransformerTest {

  @Autowired
  private BackendRegistry backendRegistry;

  @Mock
  private DataFetchingFieldSelectionSet selectionSet;

  private final NodeTransformer nodeTransformer = new NodeTransformer();

  private Model shapeModel;

  @BeforeEach
  void setUp() {
    @Cleanup RepositoryConnection con = ((Rdf4jBackend) backendRegistry
        .get(LocalBackend.LOCAL_BACKEND_NAME))
        .getRepository()
        .getConnection();

    shapeModel = QueryResults.asModel(con.getStatements(null, null, null, SHAPE_GRAPH));
  }

  @Test
  void transform_ReturnsEmptyList_ForNoSubjects() {
    // Arrange
    Model model = new ModelBuilder().build();
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE);
    List<SelectedField> selectedFields = ImmutableList.of(
        mockSelectedField(BUILDING_IDENTIFIER_FIELD, Scalars.GraphQLID));
    when(selectionSet.getFields()).thenReturn(selectedFields);

    // Act
    List<Map<String, Object>> result = nodeTransformer
        .transform(model, ImmutableList.of(), nodeShape, selectionSet);

    // Assert
    assertThat(result.isEmpty(), is(equalTo(true)));
  }

  @Test
  void transform_ReturnsSingleItemList_ForSingleSubject() {
    // Arrange
    Model model = new ModelBuilder()
        .add(BUILDING_EXAMPLE_1, BUILDING_IDENTIFIER_PATH, BUILDING_IDENTIFIER_EXAMPLE_1)
        .build();
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE);
    List<SelectedField> selectedFields = ImmutableList.of(
        mockSelectedField(BUILDING_IDENTIFIER_FIELD, Scalars.GraphQLID));
    when(selectionSet.getFields()).thenReturn(selectedFields);

    // Act
    List<Map<String, Object>> result = nodeTransformer
        .transform(model, ImmutableList.of(BUILDING_EXAMPLE_1), nodeShape, selectionSet);

    // Assert
    assertThat(result.size(), is(equalTo(1)));
    assertThat(result, hasItems(
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD,
            Optional.of(BUILDING_IDENTIFIER_EXAMPLE_1.stringValue()))));
  }

  @Test
  void transform_ReturnsMultiItemList_ForMultipleSubjects() {
    // Arrange
    Model model = new ModelBuilder()
        .add(BUILDING_EXAMPLE_1, BUILDING_IDENTIFIER_PATH, BUILDING_IDENTIFIER_EXAMPLE_1)
        .add(BUILDING_EXAMPLE_2, BUILDING_IDENTIFIER_PATH, BUILDING_IDENTIFIER_EXAMPLE_2)
        .build();
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE);
    List<SelectedField> selectedFields = ImmutableList.of(
        mockSelectedField(BUILDING_IDENTIFIER_FIELD, Scalars.GraphQLID));
    when(selectionSet.getFields()).thenReturn(selectedFields);

    // Act
    List<Map<String, Object>> result = nodeTransformer
        .transform(model, ImmutableList.of(BUILDING_EXAMPLE_1, BUILDING_EXAMPLE_2), nodeShape,
            selectionSet);

    // Assert
    assertThat(result.size(), is(equalTo(2)));
    assertThat(result, hasItems(
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD,
            Optional.of(BUILDING_IDENTIFIER_EXAMPLE_1.stringValue())),
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD,
            Optional.of(BUILDING_IDENTIFIER_EXAMPLE_2.stringValue()))));
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
