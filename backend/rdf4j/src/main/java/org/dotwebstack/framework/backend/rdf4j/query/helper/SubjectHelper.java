package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import java.util.Objects;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class SubjectHelper {

  private SubjectHelper() {}

  public static Variable getSubjectForField(Edge match, NodeShape nodeShape, FieldPath fieldPath) {
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
