package org.dotwebstack.framework.backend.rdf4j.graphql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import graphql.schema.GraphQLObjectType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphqlObjectShapeRegistryTest {

  private GraphqlObjectShapeRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new GraphqlObjectShapeRegistry();
  }

  @Test
  void register_addsBackendToRegistry() {
    // Arrange
    GraphQLObjectType objectType = GraphQLObjectType.newObject().name("foo").build();
    NodeShape nodeShape = NodeShape.builder().build();

    // Act
    registry.register(objectType, nodeShape);

    // Assert
    assertThat(registry.get(objectType), is(equalTo(nodeShape)));
  }

  @Test
  void get_returnsNull_ForAbsentBackend() {
    // Arrange
    GraphQLObjectType objectType = GraphQLObjectType.newObject().name("foo").build();

    // Act
    NodeShape nodeShape = registry.get(objectType);

    // Assert
    assertThat(nodeShape, is(nullValue()));
  }

}
