package org.dotwebstack.framework.backend.rdf4j.query.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Getter
@Setter
@Builder
class Edge {

  private RdfPredicate predicate;

  private Vertice object;

  private boolean isOptional;

  private boolean isVisible;

}
