package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_TYPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SHAPE_PREFIX;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import graphql.schema.GraphQLObjectType;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NodeShapeRegistryTest {

  @Mock
  private GraphQLObjectType objectType;

  @Mock
  private NodeShape nodeShape;

  private NodeShapeRegistry nodeShapeRegistry;

  @BeforeEach
  void setUp() {
    nodeShapeRegistry = new NodeShapeRegistry(SHAPE_PREFIX);
  }

  @Test
  void register_addsNodeShapeToRegistry() {
    // Act
    nodeShapeRegistry.register(BUILDING_SHAPE, nodeShape);

    // Assert
    assertThat(nodeShapeRegistry.get(BUILDING_SHAPE), is(equalTo(nodeShape)));
  }

  @Test
  void get_returnsNull_ForAbsentNodeShape() {
    // Act
    NodeShape nodeShape = nodeShapeRegistry.get(RDF.NIL);

    // Assert
    assertThat(nodeShape, is(nullValue()));
  }

  @Test
  void get_returnsNodeShape_ForGivenObjectType() {
    // Arrange
    GraphQLObjectType objectType = GraphQLObjectType.newObject()
        .name(BUILDING_TYPE)
        .build();

    // Act
    NodeShape nodeShape = nodeShapeRegistry.get(objectType);

    // Assert
    assertThat(nodeShape, is(equalTo(nodeShape)));
  }

}
