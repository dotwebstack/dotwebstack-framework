package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import graphql.schema.GraphQLObjectType;
import org.dotwebstack.framework.backend.rdf4j.Constants;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NodeShapeRegistryTest {

  @Mock
  private NodeShape nodeShape;

  private NodeShapeRegistry nodeShapeRegistry;

  @BeforeEach
  void setUp() {
    nodeShapeRegistry = new NodeShapeRegistry(Constants.SHAPE_PREFIX);
  }

  @Test
  void register_addsNodeShapeToRegistry() {
    nodeShapeRegistry.register(Constants.BREWERY_SHAPE, nodeShape);

    MatcherAssert.assertThat(nodeShapeRegistry.get(Constants.BREWERY_SHAPE), is(equalTo(nodeShape)));
  }

  @Test
  void get_returnsNull_ForAbsentNodeShape() {
    NodeShape nodeShape = nodeShapeRegistry.get(RDF.NIL);

    assertThat(nodeShape, is(nullValue()));
  }

  @Test
  void get_returnsNodeShape_ForGivenObjectType() {
    nodeShapeRegistry.register(Constants.BREWERY_SHAPE, nodeShape);

    GraphQLObjectType objectType = GraphQLObjectType.newObject()
        .name(Constants.BREWERY_TYPE)
        .build();

    NodeShape nodeShape = nodeShapeRegistry.get(objectType);

    assertThat(nodeShape, is(equalTo(nodeShape)));
  }

}
