package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;

@Data
@AllArgsConstructor
class OrderContext {
  private List<Field> fields;

  private Orderable orderable;


  @Data
  @AllArgsConstructor
  public static class Field {
    private String fieldName;

    private PropertyShape propertyShape;
  }
}
