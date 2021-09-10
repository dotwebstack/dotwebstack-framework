package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode
@SuperBuilder
@ToString
public class InFilterCriteria implements FilterCriteria {
  private final FieldPath fieldPath;

  @Getter
  private final List<?> values;

  @Override
  public FieldPath getFieldPath() {
    return fieldPath;
  }

}
