package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.GraphQLDirectiveContainer;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

class VerticeFactoryHelper {

  private VerticeFactoryHelper() {}

  /*
   * Check out if it is possible to go one level deeper, If not return the current nodeshape, but only
   * if we are certain that this is the last path that we process
   */
  static NodeShape getNextNodeShape(NodeShape nodeShape, String[] fieldPaths) {
    NodeShape childShape = nodeShape.getPropertyShape(fieldPaths[0])
        .getNode();
    if (Objects.isNull(childShape)) {
      if (fieldPaths.length > 1) {
        // this means that we have found a scalar field -> we cannot go any level deeper anymore
        throw illegalArgumentException("Cannot get child shape '{}' from '{}'", String.join(".", fieldPaths),
            nodeShape.getIdentifier()
                .stringValue());
      }
      return nodeShape;
    }
    return childShape;
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

  /*
   * Find out of given edge, is of any of the given types
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

  static String getFieldName(GraphQLDirectiveContainer container) {
    return Objects.nonNull(container.getDirective(CoreDirectives.FILTER_NAME)
        .getArgument(CoreDirectives.FILTER_ARG_FIELD)
        .getValue())
            ? (String) container.getDirective(CoreDirectives.FILTER_NAME)
                .getArgument(CoreDirectives.FILTER_ARG_FIELD)
                .getValue()
            : container.getName();
  }

  static Variable getSubjectForField(Edge match, NodeShape nodeShape, String[] fieldPaths) {
    if (fieldPaths.length == 1) {
      return match.getObject()
          .getSubject();
    }

    PropertyShape propertyShape = nodeShape.getPropertyShape(fieldPaths[0]);
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

    return getSubjectForField(next, propertyShape.getNode(), ArrayUtils.remove(fieldPaths, 0));
  }
}
