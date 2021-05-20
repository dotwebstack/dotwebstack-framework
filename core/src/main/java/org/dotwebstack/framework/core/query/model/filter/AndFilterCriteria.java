package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

@Data
@Builder
public class AndFilterCriteria implements FilterCriteria {
  private List<FilterCriteria> filterCriterias;

  @Override
  public FieldConfiguration getField() {
    throw unsupportedOperationException("AndFilterCriteria doesn't match a field!");
  }
}
