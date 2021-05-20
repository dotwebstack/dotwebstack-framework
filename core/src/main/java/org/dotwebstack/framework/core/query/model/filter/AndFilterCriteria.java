package org.dotwebstack.framework.core.query.model.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
public class AndFilterCriteria implements FilterCriteria {
  private List<FilterCriteria> filterCriterias;

  @Override
  public FieldConfiguration getField() {
    throw unsupportedOperationException("AndFilterCriteria doesn't match a field!");
  }
}
