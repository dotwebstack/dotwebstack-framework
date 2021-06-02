package org.dotwebstack.framework.core.query.model.filter;

import org.dotwebstack.framework.core.config.FieldConfiguration;

public interface FilterCriteria {

  default boolean isCompositeFilter() {
    return getFieldPath().length > 0;
  }

  FieldConfiguration getField();

  String[] getFieldPath();

}
