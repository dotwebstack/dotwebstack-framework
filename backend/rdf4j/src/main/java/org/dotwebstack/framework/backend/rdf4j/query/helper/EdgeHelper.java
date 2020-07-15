package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static java.util.Objects.nonNull;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.ConstraintHelper.hasConstraintOfType;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.VerticeHelper.buildVertice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.query.model.Aggregate;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.FilterRule;
import org.dotwebstack.framework.backend.rdf4j.query.model.PathType;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

public class EdgeHelper {

  private EdgeHelper() {}

  public static boolean hasEqualQueryString(Edge edge, PropertyShape propertyShape) {
    String queryString = propertyShape.getPath()
        .toPredicate()
        .getQueryString();
    return edge.getPredicate()
        .getQueryString()
        .equals(queryString);
  }

  public static boolean hasEqualTargetClass(Vertice vertice, NodeShape nodeShape) {
    return Objects.isNull(nodeShape) || hasConstraintOfType(vertice, nodeShape.getClasses());
  }

  public static boolean isEqualToEdge(PropertyShape propertyShape, Edge edge) {
    return hasEqualQueryString(edge, propertyShape) && hasEqualTargetClass(edge.getObject(), propertyShape.getNode());
  }

  public static Edge buildEdge(Variable object, PropertyShape propertyShape, boolean isVisible, boolean isOptional,
      PathType pathType) {
    return buildEdge(propertyShape, buildVertice(object, propertyShape.getNode()), isVisible, isOptional, pathType,
        null);
  }

  public static Edge buildEdge(PropertyShape propertyShape, Vertice object, boolean isVisible, boolean isOptional,
      PathType pathType, Aggregate aggregate) {
    return buildEdge(propertyShape, propertyShape.toPredicate(), propertyShape.toConstructPredicate(), object,
        isVisible, isOptional, pathType, aggregate);
  }

  private static Edge buildEdge(PropertyShape propertyShape, RdfPredicate predicate, RdfPredicate constructPredicate,
      Vertice object, boolean isVisible, boolean isOptional, PathType pathType, Aggregate aggregate) {
    return Edge.builder()
        .propertyShape(propertyShape)
        .predicate(predicate)
        .constructPredicate(constructPredicate)
        .object(object)
        .isVisible(isVisible)
        .isOptional(isOptional)
        .pathType(pathType)
        .aggregate(aggregate)
        .build();
  }

  public static Edge buildEdge(NodeShape nodeShape, FilterRule filter, Vertice childVertice, PathType pathType) {
    return Edge.builder()
        .predicate(nodeShape.getPropertyShape(filter.getFieldPath()
            .first()
            .getName())
            .getPath()
            .toPredicate())
        .object(childVertice)
        .isVisible(false)
        .isOptional(false)
        .pathType(pathType)
        .build();
  }

  public static Optional<Edge> findExistingEdge(@NonNull Vertice vertice, @NonNull PropertyShape propertyShape,
      @NonNull PathType pathType) {
    List<Edge> childEdges = nonNull(vertice.getEdges()) ? vertice.getEdges() : new ArrayList<>();

    if (!pathType.isReusePaths()) {
      return Optional.empty();
    }

    return childEdges.stream()
        .filter(childEdge -> childEdge.getPathType()
            .isReusePaths())
        .filter(childEdge -> hasEqualQueryString(childEdge, propertyShape))
        .filter(childEdge -> hasEqualTargetClass(childEdge.getObject(), propertyShape.getNode()))
        .findFirst();
  }

  public static List<Edge> deepList(List<Edge> edges) {
    return edges.stream()
        .flatMap(edge -> Stream.concat(Stream.of(edge), deepList(edge.getObject()
            .getEdges()).stream()))
        .collect(Collectors.toList());
  }
}
