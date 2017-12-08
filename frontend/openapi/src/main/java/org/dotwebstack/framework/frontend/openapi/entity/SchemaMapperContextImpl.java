package org.dotwebstack.framework.frontend.openapi.entity;


import java.util.Optional;
import lombok.Builder;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperContext;
import org.eclipse.rdf4j.model.Value;

@Builder
public class SchemaMapperContextImpl implements SchemaMapperContext {

  private Optional<Boolean> isExcludedWhenEmpty;

  private Optional<Boolean> isExcludedWhenNull;

  private Value value;

  @Override
  public Optional<Boolean> isExcludedWhenEmpty() {
    return isExcludedWhenEmpty;
  }

  @Override
  public Optional<Boolean> isExcludedWhenNull() {
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
    this.isExcludedWhenNull = Optional.of(includedWhenNull);
  }
}
