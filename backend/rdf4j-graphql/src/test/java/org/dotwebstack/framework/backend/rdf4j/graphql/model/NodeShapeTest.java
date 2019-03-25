package org.dotwebstack.framework.backend.rdf4j.graphql.model;

import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_CLASS;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_IDENTIFIER_NAME;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_IDENTIFIER_PATH;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_IDENTIFIER_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.BUILDING_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.graphql.Constants.SHAPE_GRAPH;
import static org.dotwebstack.framework.backend.rdf4j.local.LocalBackend.LOCAL_BACKEND_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dotwebstack.framework.backend.rdf4j.local.LocalBackend;
import org.dotwebstack.framework.backend.rdf4j.local.LocalBackendConfigurer;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.CoreConfiguration;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CoreConfiguration.class)
@Import(LocalBackendConfigurer.class)
class NodeShapeTest {

  @Autowired
  private BackendRegistry backendRegistry;

  private Model shapeModel;

  @BeforeEach
  void setUp() {
    LocalBackend localBackend = (LocalBackend) backendRegistry.get(LOCAL_BACKEND_NAME);
    RepositoryConnection con = localBackend.getRepository().getConnection();
    shapeModel = QueryResults.asModel(con.getStatements(null, null, null, SHAPE_GRAPH));
  }

  @Test
  void fromShapeModel_returnsNodeShape_forExistingShape() {
    // Act
    NodeShape nodeShape = NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE);

    // Assert
    assertThat(nodeShape.getIdentifier(), is(equalTo(BUILDING_SHAPE)));
    assertThat(nodeShape.getTargetClass(), is(equalTo(BUILDING_CLASS)));

    PropertyShape identifierShape = nodeShape.getPropertyShapes().get(BUILDING_IDENTIFIER_FIELD);
    assertThat(identifierShape.getIdentifier(), is(equalTo(BUILDING_IDENTIFIER_SHAPE)));
    assertThat(identifierShape.getName(), is(equalTo(BUILDING_IDENTIFIER_NAME)));
    assertThat(identifierShape.getPath(), is(equalTo(BUILDING_IDENTIFIER_PATH)));
    assertThat(identifierShape.getMinCount(), is(equalTo(1)));
    assertThat(identifierShape.getMaxCount(), is(equalTo(1)));
    assertThat(identifierShape.getNodeKind(), is(equalTo(SHACL.LITERAL)));
    assertThat(identifierShape.getDatatype(), is(equalTo(XMLSchema.STRING)));
  }

  @Test
  void fromShapeModel_throwsException_forNodeWithoutTargetClass() {
    // Arrange
    shapeModel.remove(BUILDING_SHAPE, SHACL.TARGET_CLASS, BUILDING_CLASS, SHAPE_GRAPH);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE));
  }

  @Test
  void fromShapeModel_throwsException_forLiteralPropertyWithoutDatatype() {
    // Arrange
    shapeModel.remove(BUILDING_IDENTIFIER_SHAPE, SHACL.DATATYPE, XMLSchema.STRING, SHAPE_GRAPH);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE));
  }

}
