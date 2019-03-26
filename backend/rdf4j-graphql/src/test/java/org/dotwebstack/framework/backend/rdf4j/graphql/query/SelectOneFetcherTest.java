package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_HEIGHT_EXAMPLE;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_HEIGHT_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_IDENTIFIER_EXAMPLE;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_SUBJECT;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.SHAPE_GRAPH;
import static org.dotwebstack.framework.backend.rdf4j.local.LocalBackend.LOCAL_BACKEND_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.graphql.model.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.local.LocalBackend;
import org.dotwebstack.framework.backend.rdf4j.local.LocalBackendConfigurer;
import org.dotwebstack.framework.core.BackendConfiguration;
import org.dotwebstack.framework.core.BackendRegistry;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
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
@Import(LocalBackendConfigurer.class)
class SelectOneFetcherTest {

  @Autowired
  private BackendRegistry backendRegistry;

  private RepositoryConnection con;

  private Model shapeModel;

  @BeforeEach
  void setUp() {
    LocalBackend localBackend = (LocalBackend) backendRegistry.get(LOCAL_BACKEND_NAME);
    con = localBackend.getRepository().getConnection();
    shapeModel = QueryResults.asModel(con.getStatements(null, null, null, SHAPE_GRAPH));
  }

  @Test
  void get_returnsSelectedFields_whenPresent() {
    // Arrange
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE);
    SelectOneFetcher fetcher = new SelectOneFetcher(con, nodeShape, BUILDING_SUBJECT);
    DataFetchingEnvironment environment = mockEnvironment(
        ImmutableList.of(mockSelectedField(BUILDING_IDENTIFIER_FIELD),
            mockSelectedField(BUILDING_HEIGHT_FIELD)),
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE.stringValue()));

    // Act
    BindingSet bindingSet = fetcher.get(environment);

    // Assert
    assertThat(bindingSet.getValue(BUILDING_IDENTIFIER_FIELD),
        is(equalTo(BUILDING_IDENTIFIER_EXAMPLE)));
    assertThat(bindingSet.getValue(BUILDING_HEIGHT_FIELD),
        is(equalTo(BUILDING_HEIGHT_EXAMPLE)));
  }

  private DataFetchingEnvironment mockEnvironment(List<SelectedField> selectedFields,
      Map<String, Object> arguments) {
    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);
    when(selectionSet.getFields()).thenReturn(selectedFields);

    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
    when(environment.getSelectionSet()).thenReturn(selectionSet);
    when(environment.getArguments()).thenReturn(arguments);
    arguments.forEach((argKey, argValue) ->
        when(environment.getArgument(argKey)).thenReturn(argValue));

    return environment;
  }

  private SelectedField mockSelectedField(String name) {
    SelectedField field = mock(SelectedField.class);
    when(field.getName()).thenReturn(name);

    return field;
  }

}
