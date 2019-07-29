package org.dotwebstack.framework.backend.rdf4j.query.context;

import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;


@Data
@Builder
class Edge {

  private RdfPredicate predicate;

  private RdfPredicate constructPredicate;

  private Vertice object;

  private boolean isOptional;

  private boolean isVisible;

  RdfPredicate getConstructPredicate() {
    if (Objects.nonNull(constructPredicate)) {
      return constructPredicate;
    }
    return predicate;
  }

}
