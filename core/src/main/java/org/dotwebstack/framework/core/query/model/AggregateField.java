package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.model.ObjectField;

@Builder
@Getter
public class AggregateField {
  private final ObjectField field;

  private AggregateFunctionType functionType;

  private boolean distinct;

  private String alias;

  private ScalarType type;

  private String separator;
}
