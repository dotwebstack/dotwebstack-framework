package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode
@SuperBuilder
public class GreaterThenEqualsFilterCriteria implements FilterCriteria {
  private final FieldPath fieldPath;

  @Getter
  private final Object value;

  @Override
  public List<FieldPath> getFieldPaths() {
    return List.of(fieldPath);
  }
}
