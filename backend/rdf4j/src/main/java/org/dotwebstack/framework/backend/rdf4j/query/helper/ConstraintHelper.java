package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.EdgeHelper.buildEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.EdgeHelper.findExistingEdge;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.query.model.Constraint;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.PathType;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;

public class ConstraintHelper {

  private ConstraintHelper() {}

  public static void buildRequiredEdges(Vertice vertice, Collection<PropertyShape> propertyShapes,
      OuterQuery<?> query) {
    propertyShapes.stream()
        .filter(ps -> ps.getMinCount() != null && ps.getMinCount() >= 1 && ps.getNode() != null)
        .forEach(ps -> {
          Edge edge = findExistingEdge(vertice, ps, PathType.CONSTRAINT)
              .orElseGet(() -> buildEdge(query.var(), ps, true, false, PathType.CONSTRAINT));

          edge.setOptional(false);

          vertice.getEdges()
              .add(edge);
          buildRequiredEdges(edge.getObject(), ps.getNode()
              .getPropertyShapes()
              .values(), query);
        });
  }

  public static List<Edge> resolveRequiredEdges(Collection<PropertyShape> propertyShapes, OuterQuery<?> query) {
    return propertyShapes.stream()
        .filter(ps -> ps.getMinCount() != null && ps.getMinCount() >= 1)
        .map(ps -> {
          Edge edge = buildEdge(query.var(), ps, true, false, PathType.CONSTRAINT);
          if (ps.getNode() != null) {
            buildRequiredEdges(edge.getObject(), ps.getNode()
                .getPropertyShapes()
                .values(), query);
          }
          return edge;
        })
        .collect(Collectors.toList());
  }

  /*
   * Find out if given edge contains a child edge is of given type.
   */
  static boolean hasConstraintOfType(Vertice vertice, Set<IRI> types) {
    return vertice.getConstraints(ConstraintType.RDF_TYPE)
        .stream()
        .flatMap(constraint -> constraint.getValues()
            .stream())
        .anyMatch(value -> types.stream()
            .anyMatch(value::equals));
  }

  public static Optional<Constraint> getMinCountConstraint(PropertyShape propertyShape, OuterQuery<?> outerQuery) {
    if (propertyShape.getMinCount() != null && propertyShape.getMinCount() >= 1) {
      return Optional.of(Constraint.builder()
          .predicate(propertyShape.getPath()
              .toPredicate())
          .constraintType(ConstraintType.MINCOUNT)
          .values(Set.of(outerQuery.var()))
          .build());
    } else {
      return Optional.empty();
    }
  }

  public static Optional<Constraint> getTypeConstraint(NodeShape nodeShape) {
    Set<Object> classes = new HashSet<>(nodeShape.getClasses());
    if (!classes.isEmpty()) {
      return Optional.of(Constraint.builder()
          .constraintType(ConstraintType.RDF_TYPE)
          .predicate(() -> stringify(RDF.TYPE))
          .values(classes)
          .build());
    }
    return Optional.empty();
  }

  public static Optional<Constraint> getValueConstraint(PropertyShape propertyShape) {
    if (propertyShape.getMinCount() != null && propertyShape.getMinCount() >= 1
        && propertyShape.getHasValue() != null) {
      return Optional.of(Constraint.builder()
          .predicate(propertyShape.getPath()
              .toPredicate())
          .constraintType(ConstraintType.HASVALUE)
          .values(Set.of(propertyShape.getHasValue()))
          .build());
    } else {
      return Optional.empty();
    }
  }

  /*
   * Check which edges should be added to the where part of the query based on a sh:minCount property
   * of 1
   */
  public static void buildConstraints(@NonNull Vertice vertice, @NonNull OuterQuery<?> outerQuery) {
    getTypeConstraint(vertice.getNodeShape()).ifPresent(vertice.getConstraints()::add);
    vertice.getNodeShape()
        .getPropertyShapes()
        .values()
        .forEach(ps -> {
          getValueConstraint(ps).ifPresent(vertice.getConstraints()::add);
          getMinCountConstraint(ps, outerQuery).ifPresent(minCountConstraint -> {
            vertice.getConstraints()
                .add(minCountConstraint);
          });
        });

    vertice.getEdges()
        .stream()
        .filter(edge -> edge.getPropertyShape() != null)
        .forEach(edge -> {
          Vertice childVertice = edge.getObject();
          NodeShape childNodeShape = childVertice.getNodeShape();
          if (childNodeShape != null) {
            buildConstraints(childVertice, outerQuery);
          }
        });
  }
}
