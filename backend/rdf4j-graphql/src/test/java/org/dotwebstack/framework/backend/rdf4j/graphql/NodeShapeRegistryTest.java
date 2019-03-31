package org.dotwebstack.framework.backend.rdf4j.graphql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import graphql.schema.GraphQLObjectType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
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
    nodeShapeRegistry = new NodeShapeRegistry();
  }

  @Test
  void register_addsNodeShapeToRegistry() {
    // Act
    nodeShapeRegistry.register(objectType, nodeShape);

    // Assert
    assertThat(nodeShapeRegistry.get(objectType), is(equalTo(nodeShape)));
  }

  @Test
  void get_returnsNull_ForAbsentNodeShape() {
    // Act
    NodeShape nodeShape = nodeShapeRegistry.get(mock(GraphQLObjectType.class));

    // Assert
    assertThat(nodeShape, is(nullValue()));
  }

}
