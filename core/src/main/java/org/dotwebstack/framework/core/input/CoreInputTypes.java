package org.dotwebstack.framework.core.input;


public class CoreInputTypes {

  public static final String SORT_FIELD;

  public static final String SORT_ORDER;

  public static final String SORT_FIELD_ORDER;

  public static final String SORT_FIELD_FIELD;

  private CoreInputTypes() {}

  static {
    SORT_FIELD = "SortField";
    SORT_ORDER = "SortOrder";
    SORT_FIELD_FIELD = "field";
    SORT_FIELD_ORDER = "order";
  }
}
