package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.GraphQLDirectiveContainer;
import java.util.List;
import java.util.Objects;
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
  static boolean hasChildEdgeOfType(Edge edge, IRI type) {
    List<Edge> childEdges = edge.getObject()
        .getEdges();

    return childEdges.stream()
        .anyMatch(childEdge -> (stringify(RDF.TYPE)).equals(childEdge.getPredicate()
            .getQueryString()) && (stringify(type)).equals(
                childEdge.getObject()
                    .getIri()
                    .getQueryString()));
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

  public static Variable getSubjectForField(Edge match, NodeShape nodeShape, String[] fieldPaths) {
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

  public static String stringify(IRI iri) {
    return "<" + iri.stringValue() + ">";
  }
}
