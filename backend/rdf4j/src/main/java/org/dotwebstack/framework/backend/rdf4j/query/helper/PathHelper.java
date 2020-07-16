package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static java.util.Optional.of;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.AggregateHelper.resolveAggregate;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.EdgeHelper.buildEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.EdgeHelper.findExistingEdge;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.PathType;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;

public class PathHelper {

  private PathHelper() {}

  /*
   * Find the next edge based on the given fieldpath. In case no or a only partial path is found,
   * create the missing part of the path
   */
  public static Optional<Edge> resolvePath(@NonNull Vertice vertice, NodeShape nodeShape, @NonNull FieldPath fieldPath,
      @NonNull OuterQuery<?> query, @NonNull PathType pathType) {
    if (fieldPath.last()
        .map(fieldDefinition -> fieldDefinition.getDirective(Rdf4jDirectives.RESOURCE_NAME))
        .isPresent()) {
      return Optional.empty();
    }

    if (Objects.isNull(nodeShape)) {
      return Optional.empty();
    }

    PropertyShape propertyShape =
        nodeShape.getPropertyShape(FieldPathHelper.getFirstName(fieldPath.getFieldDefinitions()));
    Edge match = findExistingEdge(vertice, propertyShape, pathType).orElseGet(() -> {
      Edge edge = buildEdge(query.var(), propertyShape, pathType);
      vertice.getEdges()
          .add(edge);
      return edge;
    });
    match.addPathType(pathType);

    if (fieldPath.isSingleton()) {
      resolveAggregate(fieldPath.first(), query.var()).ifPresent(match::setAggregate);
      return of(match);
    }

    return resolvePath(match.getObject(), nodeShape.getChildNodeShape(fieldPath.getFieldDefinitions())
        .orElse(nodeShape),
        fieldPath.rest()
            .orElseThrow(() -> illegalStateException("Remainder expected but got nothing!")),
        query, pathType);
  }
}
