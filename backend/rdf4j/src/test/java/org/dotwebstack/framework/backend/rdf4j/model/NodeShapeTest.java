package org.dotwebstack.framework.backend.rdf4j.model;

import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_CLASS;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_IDENTIFIER_NAME;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_IDENTIFIER_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_IDENTIFIER_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SHAPE_GRAPH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dotwebstack.framework.backend.rdf4j.Rdf4jConfiguration;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {Rdf4jConfiguration.class})
class NodeShapeTest {

  @Autowired
  private RepositoryConnection repositoryConnection;

  @Test
  void fromShapeModel_returnsNodeShape_forExistingShape() {
    // Arrange
    Model shapeModel = QueryResults
        .asModel(repositoryConnection.getStatements(null, null, null, SHAPE_GRAPH));

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
    Model shapeModel = QueryResults
        .asModel(repositoryConnection.getStatements(null, null, null, SHAPE_GRAPH));
    shapeModel.remove(BUILDING_SHAPE, SHACL.TARGET_CLASS, BUILDING_CLASS, SHAPE_GRAPH);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE));
  }

  @Test
  void fromShapeModel_throwsException_forLiteralPropertyWithoutDatatype() {
    // Arrange
    Model shapeModel = QueryResults
        .asModel(repositoryConnection.getStatements(null, null, null, SHAPE_GRAPH));
    shapeModel.remove(BUILDING_IDENTIFIER_SHAPE, SHACL.DATATYPE, XMLSchema.STRING, SHAPE_GRAPH);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        NodeShape.fromShapeModel(shapeModel, BUILDING_SHAPE));
  }

}
