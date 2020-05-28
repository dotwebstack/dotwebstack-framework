package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.hasChildEdgeOfType;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.hasSameType;

import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
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

  static boolean hasEqualTargetClass(Edge edge, PropertyShape propertyShape) {
    return Objects.isNull(propertyShape.getNode()) || hasChildEdgeOfType(edge, propertyShape.getNode()
        .getTargetClasses());
  }

  static boolean isEqualToEdge(PropertyShape propertyShape, Edge edge) {
    return hasEqualQueryString(edge, propertyShape) && hasEqualTargetClass(edge, propertyShape);
  }

  private static boolean isEqualEdge(Edge edge1, Edge edge2) {
    boolean queryStringEquals = edge1.getPredicate()
        .getQueryString()
        .equals(edge2.getPredicate()
            .getQueryString());
    boolean typeEquals = hasSameType(edge1, edge2);
    return queryStringEquals && typeEquals;
  }

  private static Consumer<Edge> addToDuplicate(Edge edge) {
    List<Edge> childEdges = edge.getObject()
        .getEdges();

    return duplicate -> duplicate.getObject()
        .getEdges()
        .addAll(childEdges);
  }


  static Edge getNewEdge(PropertyShape propertyShape, Vertice vertice, boolean required, boolean isVisible,
      OuterQuery<?> query) {
    Edge newEdge = createSimpleEdge(query.var(), propertyShape.getPath(), !required, isVisible);
    List<Edge> edges = vertice.getEdges();
    edges.add(newEdge);
    return newEdge;
  }

  /*
   * It can happen that the same path is used twice, we want to overcome this problem, by looking at
   * the edges, if we find more edges with the same predicate, we place the child edges of the latter
   * edges we find, on top of the first edge we find.
   */
  static List<Edge> makeEdgesUnique(List<Edge> edges) {
    List<Edge> uniqueEdges = new ArrayList<>();
    edges.forEach(edge -> uniqueEdges.stream()
        .filter(uniqueEdge -> isEqualEdge(uniqueEdge, edge))
        .findFirst()
        .ifPresentOrElse(addToDuplicate(edge), () -> uniqueEdges.add(edge)));
    return uniqueEdges;
  }

  static List<Edge> findEdgesToBeProcessed(NodeShape nodeShape, SelectedField field, List<Edge> edges) {
    return edges.stream()
        .filter(edge -> hasEqualQueryString(edge, nodeShape.getPropertyShape(field.getName())))
        .filter(edge -> isScalarOrHasChildOfType(edge, nodeShape, field))
        .collect(Collectors.toList());
  }

  private static boolean isScalarOrHasChildOfType(Edge edge, NodeShape nodeShape, SelectedField field) {
    return GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapAll(field.getFieldDefinition()
        .getType())) || hasChildEdgeOfType(edge, nodeShape.getPropertyShape(field.getName())
            .getNode()
            .getTargetClasses());
  }

  private static Edge buildEdge(RdfPredicate predicate, RdfPredicate constructPredicate, Vertice object,
      boolean isVisible, boolean isOptional) {
    return Edge.builder()
        .predicate(predicate)
        .constructPredicate(constructPredicate)
        .object(object)
        .isVisible(isVisible)
        .isOptional(isOptional)
        .build();
  }

  static Edge createSimpleEdge(Variable subject, BasePath basePath, boolean isOptional, boolean isVisible) {
    return buildEdge(basePath.toPredicate(), basePath.toConstructPredicate(), createVertice(subject, new HashSet<>()),
        isVisible, isOptional);
  }

  static Edge createSimpleEdge(Variable subject, Set<Iri> iris, RdfPredicate predicate, boolean isVisible) {
    return buildEdge(predicate, null, createVertice(subject, iris), isVisible, false);
  }


  private static Vertice createVertice(Variable subject, Set<Iri> iris) {
    return Vertice.builder()
        .subject(subject)
        .iris(iris)
        .build();
  }

  static List<Edge> deepList(List<Edge> edges) {
    return edges.stream()
        .flatMap(edge -> Stream.concat(Stream.of(edge), deepList(edge.getObject()
            .getEdges()).stream()))
        .collect(Collectors.toList());
  }
}
