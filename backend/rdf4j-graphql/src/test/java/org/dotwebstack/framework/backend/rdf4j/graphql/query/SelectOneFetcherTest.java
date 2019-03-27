package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import static org.dotwebstack.framework.backend.rdf4j.LocalBackend.LOCAL_BACKEND_NAME;
import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_EXAMPLE;
import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_HEIGHT_EXAMPLE;
import static org.dotwebstack.framework.test.Constants.BUILDING_HEIGHT_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_2;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_SHAPE;
import static org.dotwebstack.framework.test.Constants.BUILDING_SUBJECT;
import static org.dotwebstack.framework.test.Constants.SHAPE_GRAPH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetchingEnvironment;
import org.dotwebstack.framework.backend.rdf4j.LocalBackend;
import org.dotwebstack.framework.backend.rdf4j.LocalBackendConfigurer;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.BackendConfiguration;
import org.dotwebstack.framework.core.BackendRegistry;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = BackendConfiguration.class)
@Import(LocalBackendConfigurer.class)
class SelectOneFetcherTest extends AbstractFetcherTest {

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
            mockSelectedField(BUILDING_HEIGHT_FIELD), mockSelectedField(BUILDING_BUILT_AT_FIELD)),
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE_1.stringValue()));

    // Act
    BindingSet bindingSet = fetcher.get(environment);

    // Assert
    assertThat(bindingSet.getValue(BUILDING_IDENTIFIER_FIELD),
        is(equalTo(BUILDING_IDENTIFIER_EXAMPLE_1)));
    assertThat(bindingSet.getValue(BUILDING_HEIGHT_FIELD),
        is(equalTo(BUILDING_HEIGHT_EXAMPLE)));
    assertThat(bindingSet.getValue(BUILDING_BUILT_AT_FIELD),
        is(equalTo(BUILDING_BUILT_AT_EXAMPLE)));
  }

  @Test
  void get_returnsNullableFields_whenOptional() {
    // Arrange
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE);
    SelectOneFetcher fetcher = new SelectOneFetcher(con, nodeShape, BUILDING_SUBJECT);
    DataFetchingEnvironment environment = mockEnvironment(
        ImmutableList.of(mockSelectedField(BUILDING_IDENTIFIER_FIELD),
            mockSelectedField(BUILDING_HEIGHT_FIELD), mockSelectedField(BUILDING_BUILT_AT_FIELD)),
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE_2.stringValue()));

    // Act
    BindingSet bindingSet = fetcher.get(environment);

    // Assert
    assertThat(bindingSet.getValue(BUILDING_IDENTIFIER_FIELD),
        is(equalTo(BUILDING_IDENTIFIER_EXAMPLE_2)));
    assertThat(bindingSet.getValue(BUILDING_BUILT_AT_FIELD), is(nullValue()));
  }

  @Test
  void get_returnsNull_whenAbsent() {
    // Arrange
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE);
    SelectOneFetcher fetcher = new SelectOneFetcher(con, nodeShape, BUILDING_SUBJECT);
    DataFetchingEnvironment environment = mockEnvironment(
        ImmutableList.of(mockSelectedField(BUILDING_IDENTIFIER_FIELD),
            mockSelectedField(BUILDING_HEIGHT_FIELD), mockSelectedField(BUILDING_BUILT_AT_FIELD)),
        ImmutableMap.of(BUILDING_IDENTIFIER_FIELD, "foo"));

    // Act
    BindingSet bindingSet = fetcher.get(environment);

    // Assert
    assertThat(bindingSet, CoreMatchers.is(nullValue()));
  }

}
