package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

public final class PropertyShape2 {

  private final IRI dataType;

  private final Value defaultValue;

  public PropertyShape2(@NonNull IRI dataType, Value defaultValue) {
    this.dataType = dataType;
    this.defaultValue = defaultValue;
  }

  public IRI getDataType() {
    return dataType;
  }

  public Value getDefaultValue() {
    return defaultValue;
  }

}
