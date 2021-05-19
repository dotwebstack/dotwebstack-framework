package org.dotwebstack.framework.core.query.model;

import static org.dotwebstack.framework.core.query.model.AggregateFunctionType.JOIN;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Builder
@Data
public class AggregateObjectFieldConfiguration {
  private final FieldConfiguration field;

  private final List<AggregateFieldConfiguration> aggregateFields;

  public List<AggregateFieldConfiguration> getAggregateFields(boolean stringJoin) {
    return aggregateFields.stream()
        .filter(
            aggregateFieldConfiguration -> stringJoin ? aggregateFieldConfiguration.getAggregateFunctionType() == JOIN
                : aggregateFieldConfiguration.getAggregateFunctionType() != JOIN)
        .collect(Collectors.toList());

  }
}
