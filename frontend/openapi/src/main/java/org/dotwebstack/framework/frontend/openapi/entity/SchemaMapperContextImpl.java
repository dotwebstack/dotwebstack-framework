package org.dotwebstack.framework.frontend.openapi.entity;

import lombok.Builder;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperContext;
import org.eclipse.rdf4j.model.Value;

@Builder
public class SchemaMapperContextImpl implements SchemaMapperContext {

  private Boolean isExcludedWhenEmpty = true;

  private Boolean isExcludedWhenNull = true;

  private Value value;

  @Override
  public Boolean isExcludedWhenEmpty() {
    return isExcludedWhenEmpty;
  }

  @Override
  public Boolean isExcludedWhenNull() {
    return isExcludedWhenNull;
  }

  @Override
  public Value getValue() {
    return value;
  }

  @Override
  public void setValue(Value value) {
    this.value = value;
  }

  @Override
  public void setExcludedWhenNull(boolean includedWhenNull) {
    this.isExcludedWhenNull = includedWhenNull;
  }

  @Override
  public void setExcludedWhenEmpty(boolean includedWhenEmpty) {
    this.isExcludedWhenEmpty = includedWhenEmpty;
  }
}
