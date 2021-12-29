package org.dotwebstack.framework.core.datafetchers.filter;

import org.apache.commons.lang3.EnumUtils;

public enum FilterOperator {
  EQ, IN, LT, LTE, GT, GTE, CONTAINS_ALL_OF, CONTAINS_ANY_OF, NOT, CONTAINS, WITHIN, INTERSECTS, MATCH, SRID, _EXISTS;

  public static FilterOperator getFilterOperator(String name) {
    if (FilterConstants.CONTAINS_ALL_OF_FIELD.equals(name)) {
      return CONTAINS_ALL_OF;
    }
    if (FilterConstants.CONTAINS_ANY_OF_FIELD.equals(name)) {
      return CONTAINS_ANY_OF;
    }
    return EnumUtils.getEnumIgnoreCase(FilterOperator.class, name);
  }
}
