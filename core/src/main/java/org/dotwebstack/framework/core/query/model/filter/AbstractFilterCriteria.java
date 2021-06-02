package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

@SuperBuilder
@Data
@RequiredArgsConstructor
public abstract class AbstractFilterCriteria implements FilterCriteria {
  @Builder.Default
  protected final String fieldPath = "";

  public String[] getFieldPath() {
    return StringUtils.split(fieldPath, '.');
  }
}
