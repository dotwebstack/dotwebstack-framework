package org.dotwebstack.framework.core.query.model.filter;

import org.dotwebstack.framework.core.config.FieldConfiguration;

public interface FilterCriteria {

  boolean isCompositeFilter = false;

  FieldConfiguration getField();

}
