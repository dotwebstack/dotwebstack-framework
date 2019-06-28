package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.List;
import lombok.Data;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;

@Data
class OrderContext {
  private List<Field> fields;

  private Orderable orderable;

  public OrderContext(List<Field> fields, Orderable orderable) {
    this.fields = fields;
    this.orderable = orderable;
  }

  @Data
  public static class Field {
    private String fieldName;

    private PropertyShape propertyShape;

    public Field(String fieldName, PropertyShape propertyShape) {
      this.fieldName = fieldName;
      this.propertyShape = propertyShape;
    }
  }
}
