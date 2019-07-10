package org.dotwebstack.framework.backend.rdf4j.query.context;

import lombok.Data;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

@Data
public class OrderContext {

  private Variable field;

  private Orderable orderable;

  public OrderContext(Variable field, Orderable orderable) {
    this.field = field;
    this.orderable = orderable;
  }
}
