package org.dotwebstack.framework.backend.rdf4j.graphql.shacl;

import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.stereotype.Component;

@Component
public class NodeTransformer {

  public List<Map<String, Object>> transform(Model model, List<IRI> subjects,
      NodeShape nodeShape, DataFetchingFieldSelectionSet selectionSet) {
    return subjects.stream()
        .map(subject -> buildNode(model, subject, nodeShape, selectionSet))
        .collect(Collectors.toList());
  }

  private static Map<String, Object> buildNode(Model model, IRI subject, NodeShape nodeShape,
      DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getFields().stream()
        .map(field -> nodeShape.getPropertyShape(field.getName()))
        .collect(Collectors.toMap(PropertyShape::getName, propertyShape -> Models
            .getProperty(model, subject, propertyShape.getPath())
            .map(ValueUtils::convertValue)));
  }

}
