package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Builder
@Data
public class AggregateObjectFieldConfiguration {
  private final FieldConfiguration field;

  private final List<AggregateFieldConfiguration> aggregateFields;
}
