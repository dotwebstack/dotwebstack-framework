package org.dotwebstack.framework.backend.rdf4j.graphql;

import graphql.schema.GraphQLObjectType;
import java.util.HashMap;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.springframework.stereotype.Component;

@Component
public class GraphqlObjectShapeRegistry {

  private HashMap<GraphQLObjectType, NodeShape> objectShapes = new HashMap<>();

  public void register(GraphQLObjectType objectType, NodeShape backend) {
    objectShapes.put(objectType, backend);
  }

  public NodeShape get(GraphQLObjectType objectType) {
    return objectShapes.get(objectType);
  }

}

