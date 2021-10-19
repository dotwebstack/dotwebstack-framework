package org.dotwebstack.framework.core.query.model;

import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.model.ObjectField;

@Builder
@Getter
public class AggregateObjectRequest {
  private final ObjectField objectField;

  private final List<AggregateField> aggregateFields;

  public List<AggregateField> getAggregateFields(boolean stringJoin) {
    return aggregateFields.stream()
        .filter(aggregateField -> stringJoin == (aggregateField.getFunctionType() == JOIN))
        .collect(Collectors.toList());

  }
}
