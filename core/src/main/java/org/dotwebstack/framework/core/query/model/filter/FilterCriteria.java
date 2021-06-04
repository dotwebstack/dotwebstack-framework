package org.dotwebstack.framework.core.query.model.filter;


public interface FilterCriteria {

  default boolean isNestedFilter() {
    return getFieldPath() != null && !getFieldPath().isLeaf();
  }

  FieldPath getFieldPath();
}
