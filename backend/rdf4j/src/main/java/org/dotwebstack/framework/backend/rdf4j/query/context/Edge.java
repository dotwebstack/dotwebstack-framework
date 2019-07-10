package org.dotwebstack.framework.backend.rdf4j.query.context;

import lombok.Builder;
import lombok.Data;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Data
@Builder
class Edge {

  private RdfPredicate predicate;

  private Vertice object;

  private boolean isOptional;

  private boolean isVisible;

}
