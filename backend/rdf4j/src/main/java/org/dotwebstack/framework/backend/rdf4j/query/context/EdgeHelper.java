package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.hasConstraintOfType;

import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

class EdgeHelper {

  private EdgeHelper() {}

  static boolean hasEqualQueryString(Edge edge, PropertyShape propertyShape) {
    String queryString = propertyShape.getPath()
        .toPredicate()
        .getQueryString();
    return edge.getPredicate()
        .getQueryString()
        .equals(queryString);
  }

  static boolean hasEqualTargetClass(Vertice vertice, NodeShape nodeShape) {
    return Objects.isNull(nodeShape) || hasConstraintOfType(vertice, nodeShape.getClasses());
  }

  static boolean isEqualToEdge(PropertyShape propertyShape, Edge edge) {
    return hasEqualQueryString(edge, propertyShape) && hasEqualTargetClass(edge.getObject(), propertyShape.getNode());
  }

  static Edge getNewEdge(PropertyShape propertyShape, Vertice vertice, boolean isVisible, boolean required,
      OuterQuery<?> query) {
    Edge newEdge = createSimpleEdge(query.var(), propertyShape, isVisible, !required);
    List<Edge> edges = vertice.getEdges();
    edges.add(newEdge);
    return newEdge;
  }

  static List<Edge> findEdgesToBeProcessed(NodeShape nodeShape, SelectedField field, List<Edge> edges) {
    return edges.stream()
        .filter(edge -> hasEqualQueryString(edge, nodeShape.getPropertyShape(field.getName())))
        .filter(edge -> isScalarOrHasChildOfType(edge, nodeShape, field))
        .collect(Collectors.toList());
  }

  private static boolean isScalarOrHasChildOfType(Edge edge, NodeShape nodeShape, SelectedField field) {
    return GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapAll(field.getFieldDefinition()
        .getType())) || hasConstraintOfType(edge.getObject(), nodeShape.getPropertyShape(field.getName())
            .getNode()
            .getClasses());
  }

  static Edge createSimpleEdge(Variable object, PropertyShape propertyShape, boolean isVisible, boolean isOptional) {
    return createEdge(propertyShape, createVertice(object, propertyShape.getNode()), isVisible, isOptional);
  }



  private static Edge createEdge(PropertyShape propertyShape, Vertice object, boolean isVisible, boolean isOptional) {
    return createEdge(propertyShape, propertyShape.getPath()
        .toPredicate(),
        propertyShape.getPath()
            .toConstructPredicate(),
        object, isVisible, isOptional);
  }

  private static Edge createEdge(RdfPredicate predicate, Vertice object, boolean isVisible) {
    return createEdge(null, predicate, null, object, isVisible, false);
  }

  private static Edge createEdge(PropertyShape propertyShape, RdfPredicate predicate, RdfPredicate constructPredicate,
      Vertice object, boolean isVisible, boolean isOptional) {
    return Edge.builder()
        .propertyShape(propertyShape)
        .predicate(predicate)
        .constructPredicate(constructPredicate)
        .object(object)
        .isVisible(isVisible)
        .isOptional(isOptional)
        .build();
  }


  private static Vertice createVertice(Variable subject, NodeShape nodeShape) {
    return Vertice.builder()
        .nodeShape(nodeShape)
        .subject(subject)
        .build();
  }

  static List<Edge> deepList(List<Edge> edges) {
    return edges.stream()
        .flatMap(edge -> Stream.concat(Stream.of(edge), deepList(edge.getObject()
            .getEdges()).stream()))
        .collect(Collectors.toList());
  }
}
