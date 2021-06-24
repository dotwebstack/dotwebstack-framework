package org.dotwebstack.framework.core.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;

@Data
@EqualsAndHashCode(callSuper = true)
public class FieldConfigurationImpl extends AbstractFieldConfiguration {

  private boolean scalarField = false;

  private boolean objectField = false;

  private boolean nestedObjectField = false;

  public boolean isAggregate() {
    return AggregateHelper.isAggregate(this);
  }

  @Override
  public String getType() {
    if (isAggregate()) {
      return AggregateConstants.AGGREGATE_TYPE;
    }
    return super.getType();
  }

  @Override
  public boolean isScalarField() {
    return scalarField;
  }

  @Override
  public boolean isObjectField() {
    return objectField;
  }

  @Override
  public boolean isNestedObjectField() {
    return nestedObjectField;
  }

  @Override
  public boolean isAggregateField() {
    return isAggregate();
  }
}
