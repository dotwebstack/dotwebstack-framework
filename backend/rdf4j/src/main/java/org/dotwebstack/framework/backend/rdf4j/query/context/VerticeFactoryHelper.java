package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.GraphQLFieldDefinition;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

class VerticeFactoryHelper {

  private VerticeFactoryHelper() {}

  /*
   * Check out if it is possible to go one level deeper, If not return the current nodeshape, but only
   * if we are certain that this is the last path that we process
   */
  static NodeShape getNextNodeShape(NodeShape nodeShape, List<GraphQLFieldDefinition> fieldPath) {
    Optional<NodeShape> childShape = fieldPath.stream()
        .findFirst()
        .map(GraphQLFieldDefinition::getName)
        .map(nodeShape::getPropertyShape)
        .map(PropertyShape::getNode);

    if (childShape.isPresent()) {
      return childShape.get();
    }

    if (fieldPath.size() > 1) {
      // this means that we have found a scalar field -> we cannot go any level deeper anymore
      throw illegalArgumentException("Cannot get child shape '{}' from '{}'", fieldPath.stream()
          .map(GraphQLFieldDefinition::getName)
          .collect(Collectors.joining(".")),
          nodeShape.getIdentifier()
              .stringValue());
    }
    return nodeShape;
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


  static Variable getSubjectForField(Edge match, NodeShape nodeShape, FieldPath fieldPath) {
    if (fieldPath.isSingleton()) {
      return Objects.nonNull(match.getAggregate()) ? match.getAggregate()
          .getVariable()
          : match.getObject()
              .getSubject();
    }

    PropertyShape propertyShape = nodeShape.getPropertyShape(fieldPath.first()
        .getName());
    Edge next = match.getObject()
        .getEdges()
        .stream()
        .filter(edge -> edge.getPredicate()
            .equals(propertyShape.getPath()
                .toPredicate()))
        .findFirst()
        .orElseThrow(() -> illegalArgumentException("Did not find a predicate with name '{}' for edge '{}'",
            propertyShape.getPath()
                .toPredicate(),
            match.getObject()
                .getSubject()));

    return getSubjectForField(next, propertyShape.getNode(), fieldPath.rest()
        .orElseThrow(() -> illegalStateException("Expected a remainder but got nothing!")));
  }
}
