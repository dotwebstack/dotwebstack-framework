package org.dotwebstack.framework.backend.json.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
public class JsonFieldConfiguration extends AbstractFieldConfiguration {

  @Override
  public boolean isScalarField() {
    return false;
  }

  @Override
  public boolean isObjectField() {
    return false;
  }

  @Override
  public boolean isNestedObjectField() {
    return false;
  }

  @Override
  public boolean isAggregateField() {
    return false;
  }
}
