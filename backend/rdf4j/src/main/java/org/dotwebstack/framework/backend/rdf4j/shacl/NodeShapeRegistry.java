package org.dotwebstack.framework.backend.rdf4j.shacl;

import graphql.schema.GraphQLObjectType;
import java.util.Collection;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

@RequiredArgsConstructor
public class NodeShapeRegistry {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final String shapePrefix;

  private final HashMap<IRI, NodeShape> nodeShapes = new HashMap<>();

  public void register(IRI identifier, NodeShape nodeShape) {
    nodeShapes.put(identifier, nodeShape);
  }

  public Collection<NodeShape> all() {
    return nodeShapes.values();
  }

  public NodeShape get(IRI identifier) {
    return nodeShapes.get(identifier);
  }

  public NodeShape get(GraphQLObjectType objectType) {
    return get(VF.createIRI(shapePrefix, objectType.getName()));
  }

}
