package org.dotwebstack.framework.core.backend.filter;



public interface FilterCriteria {

  default boolean isGroupFilter() {
    return this instanceof GroupFilterCriteria;
  }

  default GroupFilterCriteria asGroupFilter() {
    if (isGroupFilter()) {
      return (GroupFilterCriteria) this;
    }

    throw new IllegalArgumentException("Not a group filter!");
  }

  default boolean isObjectFieldFilter() {
    return this instanceof ObjectFieldFilterCriteria;
  }

  default ObjectFieldFilterCriteria asObjectFieldFilter() {
    if (isObjectFieldFilter()) {
      return (ObjectFieldFilterCriteria) this;
    }

    throw new IllegalArgumentException("Not a scalar field filter!");
  }
}
