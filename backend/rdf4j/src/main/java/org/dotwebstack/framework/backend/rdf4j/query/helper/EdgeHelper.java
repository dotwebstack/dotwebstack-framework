package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static java.util.Objects.nonNull;
import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.ConstraintHelper.hasConstraintOfType;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.VerticeHelper.buildVertice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.query.model.Aggregate;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.FilterRule;
import org.dotwebstack.framework.backend.rdf4j.query.model.PathType;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

public class EdgeHelper {

  private EdgeHelper() {}

  public static boolean hasEqualQueryString(@NonNull Edge edge, @Nonnull PropertyShape propertyShape) {
    String queryString = propertyShape.getPath()
        .toPredicate()
        .getQueryString();
    return edge.getPredicate()
        .getQueryString()
        .equals(queryString);
  }

  public static boolean hasEqualTargetClass(@NonNull Vertice vertice, NodeShape nodeShape) {
    return Objects.isNull(nodeShape) || hasConstraintOfType(vertice, nodeShape.getClasses());
  }

  public static boolean isEqualToEdge(@NonNull PropertyShape propertyShape, @Nonnull Edge edge) {
    return hasEqualQueryString(edge, propertyShape) && hasEqualTargetClass(edge.getObject(), propertyShape.getNode());
  }

  public static Edge buildEdge(@Nonnull Variable object, @NonNull PropertyShape propertyShape,
      @Nonnull PathType pathType) {
    return buildEdge(propertyShape, buildVertice(object, propertyShape.getNode()), pathType, null);
  }

  public static Edge buildEdge(@NonNull PropertyShape propertyShape, @Nonnull Vertice object,
      @NonNull PathType pathType, Aggregate aggregate) {
    return buildEdge(propertyShape, propertyShape.toPredicate(), propertyShape.toConstructPredicate(), object, pathType,
        aggregate);
  }

  private static Edge buildEdge(PropertyShape propertyShape, RdfPredicate predicate, RdfPredicate constructPredicate,
      Vertice object, PathType pathType, Aggregate aggregate) {
    return Edge.builder()
        .propertyShape(propertyShape)
        .predicate(predicate)
        .constructPredicate(constructPredicate)
        .object(object)
        .pathTypes(new ArrayList<>(List.of(pathType)))
        .aggregate(aggregate)
        .build();
  }

  public static Edge buildEdge(@NonNull NodeShape nodeShape, @NonNull FilterRule filter, @NonNull Vertice childVertice,
      @NonNull PathType pathType) {
    return Edge.builder()
        .predicate(nodeShape.getPropertyShape(filter.getFieldPath()
            .first()
            .getName())
            .getPath()
            .toPredicate())
        .object(childVertice)
        .pathTypes(new ArrayList<>(List.of(pathType)))
        .build();
  }

  public static Edge buildEdge(@NonNull Variable subject, @NonNull PathType pathType) {
    return Edge.builder()
        .predicate(() -> stringify(RDF.TYPE))
        .object(buildVertice(subject, null))
        .pathTypes(new ArrayList<>(List.of(pathType)))
        .build();
  }

  public static Optional<Edge> findExistingEdge(@NonNull Vertice vertice, @NonNull PropertyShape propertyShape,
      @NonNull PathType pathType) {
    List<Edge> childEdges = nonNull(vertice.getEdges()) ? vertice.getEdges() : new ArrayList<>();

    if (!pathType.hasReusablePaths()) {
      return Optional.empty();
    }

    return childEdges.stream()
        .filter(Edge::hasReusablePaths)
        .filter(childEdge -> hasEqualQueryString(childEdge, propertyShape))
        .filter(childEdge -> hasEqualTargetClass(childEdge.getObject(), propertyShape.getNode()))
        .findFirst();
  }
}
