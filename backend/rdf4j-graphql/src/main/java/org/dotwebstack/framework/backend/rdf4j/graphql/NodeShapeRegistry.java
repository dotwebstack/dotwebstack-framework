package org.dotwebstack.framework.backend.rdf4j.graphql;

import graphql.schema.GraphQLObjectType;
import java.util.HashMap;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.springframework.stereotype.Component;

@Component
public class NodeShapeRegistry {

  private final HashMap<GraphQLObjectType, NodeShape> nodeShapes = new HashMap<>();

  public void register(GraphQLObjectType type, NodeShape nodeShape) {
    nodeShapes.put(type, nodeShape);
  }

  public NodeShape get(GraphQLObjectType type) {
    return nodeShapes.get(type);
  }

}
