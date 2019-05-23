package org.dotwebstack.framework.backend.rdf4j.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;

@Data
@AllArgsConstructor
class OrderContext {
  private String field;

  private Orderable orderable;

  private PropertyShape propertyShape;
}
