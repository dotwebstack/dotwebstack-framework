package org.dotwebstack.framework.core.input;


public class CoreInputTypes {

  public static final String SORT_FIELD;

  public static final String SORT_ORDER;

  public static final String SORT_FIELD_ORDER;

  public static final String SORT_FIELD_ORDER_ASC;

  public static final String SORT_FIELD_ORDER_DESC;

  public static final String SORT_FIELD_FIELD;

  public static final String AGGREGATE_TYPE;

  public static final String AGGREGATE_TYPE_COUNT;

  private CoreInputTypes() {}

  static {
    SORT_FIELD = "SortField";
    SORT_ORDER = "SortOrder";
    SORT_FIELD_ORDER = "order";
    SORT_FIELD_ORDER_ASC = "ASC";
    SORT_FIELD_ORDER_DESC = "DESC";
    SORT_FIELD_FIELD = "field";
    AGGREGATE_TYPE = "type";
    AGGREGATE_TYPE_COUNT = "COUNT";
  }
}
