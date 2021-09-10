package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@Builder
@ToString
public class AndFilterCriteria implements FilterCriteria {

  private final FieldPath fieldPath;

  @Getter
  private final List<FilterCriteria> filterCriterias;

  @Override
  public FieldPath getFieldPath() {
    return fieldPath;
  }

}
