package org.dotwebstack.framework.core.backend.filter;


import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

public interface FilterCriteria {

  default boolean isGroupFilter() {
    return this instanceof GroupFilterCriteria;
  }

  default GroupFilterCriteria asGroupFilter() {
    if (isGroupFilter()) {
      return (GroupFilterCriteria) this;
    }

    throw illegalArgumentException("Not a group filter!");
  }

  default boolean isScalarFieldFilter() {
    return this instanceof ScalarFieldFilterCriteria;
  }

  default ScalarFieldFilterCriteria asScalarFieldFilter() {
    if (isScalarFieldFilter()) {
      return (ScalarFieldFilterCriteria) this;
    }

    throw illegalArgumentException("Not a scalar field filter!");
  }
}
