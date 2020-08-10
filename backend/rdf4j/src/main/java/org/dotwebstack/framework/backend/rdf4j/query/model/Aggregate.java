package org.dotwebstack.framework.backend.rdf4j.query.model;

import lombok.Builder;
import lombok.Data;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

@Data
@Builder
public class Aggregate {
  private AggregateType type;

  private Variable variable;
}
