package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.GraphQLObjectType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class NodeShapeRegistry {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final String shapePrefix;

  private final HashMap<IRI, NodeShape> nodeShapes = new HashMap<>();

  private final HashMap<String, NodeShape> nodeShapesByName = new HashMap<>();

  public NodeShapeRegistry(String shapePrefix) {
    this.shapePrefix = shapePrefix;
  }

  public void register(IRI identifier, NodeShape nodeShape) {
    nodeShapes.put(identifier, nodeShape);
    nodeShapesByName.put(nodeShape.getName(), nodeShape);
  }

  public Collection<NodeShape> all() {
    return nodeShapes.values();
  }

  public NodeShape get(IRI identifier) {
    return nodeShapes.get(identifier);
  }

  public NodeShape get(String objectName) {
    return get(VF.createIRI(shapePrefix, objectName));
  }

  public NodeShape get(GraphQLObjectType objectType) {
    IRI iri = VF.createIRI(shapePrefix, objectType.getName());
    return Optional.ofNullable(iri)
        .map(this::get)
        .orElseThrow(() -> invalidConfigurationException("No nodeshape found for {}", iri));
  }

  public NodeShape getByShaclName(String shaclName) throws UnsupportedOperationException {
    NodeShape result = nodeShapesByName.get(shaclName);
    if (result != null) {
      return result;
    }
    throw unsupportedOperationException("Nodeshape not found by sh:name '{}'", shaclName);
  }

}
