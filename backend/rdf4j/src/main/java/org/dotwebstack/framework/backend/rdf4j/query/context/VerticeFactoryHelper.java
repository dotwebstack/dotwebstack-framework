package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.GraphQLFieldDefinition;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
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
  static boolean hasChildEdgeOfType(Edge edge, Set<IRI> types) {
    return edge.getObject()
        .getEdges()
        .stream()
        .anyMatch(childEdge -> isOfType(childEdge, types));
  }

  static boolean hasSameType(Edge edge1, Edge edge2) {
    return edge1.getObject()
        .getEdges()
        .stream()
        .filter(e1 -> stringify(RDF.TYPE).equals(e1.getPredicate()
            .getQueryString()))
        .flatMap(e1 -> e1.getObject()
            .getIris()
            .stream())
        .anyMatch(type1 -> edge2.getObject()
            .getEdges()
            .stream()
            .filter(e2 -> stringify(RDF.TYPE).equals(e2.getPredicate()
                .getQueryString()))
            .flatMap(e2 -> e2.getObject()
                .getIris()
                .stream())
            .anyMatch(type2 -> Objects.equals(type1.getQueryString(), type2.getQueryString())));
  }

  /*
   * Find out whether the given edge is of any of the given types
   */
  static boolean isOfType(Edge edge, Set<IRI> types) {
    return types.stream()
        .anyMatch(type -> (stringify(RDF.TYPE)).equals(edge.getPredicate()
            .getQueryString())
            && (edge.getObject()
                .getIris()
                .stream()
                .anyMatch(iri -> iri.getQueryString()
                    .equals(stringify(type)))));
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
