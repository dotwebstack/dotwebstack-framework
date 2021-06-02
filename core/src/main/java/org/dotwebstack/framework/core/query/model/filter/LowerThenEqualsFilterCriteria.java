package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@SuperBuilder
public class LowerThenEqualsFilterCriteria extends AbstractFilterCriteria {
  private final FieldConfiguration field;

  private final Object value;
}
