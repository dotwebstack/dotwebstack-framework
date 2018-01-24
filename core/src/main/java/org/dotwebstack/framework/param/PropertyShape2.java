package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public final class PropertyShape2 {

  private final IRI dataType;

  private final Literal defaultValue;

  public PropertyShape2(@NonNull IRI dataType, @NonNull Literal defaultValue) {
    this.dataType = dataType;
    this.defaultValue = defaultValue;
  }

  public IRI getDataType() {
    return dataType;
  }

  public Literal getDefaultValue() {
    return defaultValue;
  }

}
