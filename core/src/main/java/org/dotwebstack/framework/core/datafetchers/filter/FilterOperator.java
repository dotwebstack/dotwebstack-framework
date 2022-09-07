package org.dotwebstack.framework.core.datafetchers.filter;

import org.apache.commons.lang3.EnumUtils;

public enum FilterOperator {
  @SuppressWarnings("checkstyle:linelength")
  EQ, EQ_IGNORE_CASE, IN, IN_IGNORE_CASE, LT, LTE, GT, GTE, CONTAINS_ALL_OF, CONTAINS_ANY_OF, NOT, CONTAINS, WITHIN, INTERSECTS, TOUCHES, MATCH, SRID, TYPE, EXISTS;

  public static FilterOperator getFilterOperator(String name, boolean isCaseSensitive) {
    if (FilterConstants.CONTAINS_ALL_OF_FIELD.equals(name)) {
      return CONTAINS_ALL_OF;
    }
    if (FilterConstants.CONTAINS_ANY_OF_FIELD.equals(name)) {
      return CONTAINS_ANY_OF;
    }
    if (FilterConstants.EQ_FIELD.equals(name) && !isCaseSensitive) {
      return EQ_IGNORE_CASE;
    }
    if (FilterConstants.IN_FIELD.equals(name) && !isCaseSensitive) {
      return IN_IGNORE_CASE;
    }
    if (FilterConstants.EXISTS_FIELD.equals(name)) {
      return EXISTS;
    }

    return EnumUtils.getEnumIgnoreCase(FilterOperator.class, name);
  }
}
