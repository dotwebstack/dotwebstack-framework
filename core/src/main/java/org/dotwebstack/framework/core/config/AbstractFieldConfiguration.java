package org.dotwebstack.framework.core.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public abstract class AbstractFieldConfiguration implements FieldConfiguration {
  private String name;

  private String type;

  private boolean nullable = false;

  private boolean list = false;

  private boolean isNumeric = false;

  private boolean isText = false;

  private boolean visible = true;

  private String mappedBy;

  private String aggregationOf;

  private TypeConfiguration<?> typeConfiguration;

  private List<FieldArgumentConfiguration> arguments = new ArrayList<>();

  public abstract boolean isScalarField();

  public abstract boolean isObjectField();

  public abstract boolean isNestedObjectField();

  public abstract boolean isAggregateField();

  public boolean isVisible() {
    return visible;
  }
}
