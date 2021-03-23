package org.dotwebstack.framework.core.datafetchers.aggregate;

import java.util.Set;

public class AggregateConstants {

  private AggregateConstants() {}

  // ObjectTypes

  public static final String AGGREGATE_TYPE = "Aggregate";

  // ArgumentNames

  public static final String FIELD_ARGUMENT = "field";

  public static final String SEPARATOR_ARGUMENT = "separator";

  public static final String DISTINCT_ARGUMENT = "distinct";

  // FieldNames
  public static final String STRING_JOIN_FIELD = "stringJoin";

  public static final String COUNT_FIELD = "count";

  public static final String INT_SUM_FIELD = "intSum";

  public static final String INT_MIN_FIELD = "intMin";

  public static final String INT_MAX_FIELD = "intMax";

  public static final String INT_AVG_FIELD = "intAvg";

  public static final String FLOAT_SUM_FIELD = "floatSum";

  public static final String FLOAT_MIN_FIELD = "floatMin";

  public static final String FLOAT_MAX_FIELD = "floatMax";

  public static final String FLOAT_AVG_FIELD = "floatAvg";

  public static final Set<String> NUMERIC_FUNCTIONS = Set.of(INT_SUM_FIELD, INT_MIN_FIELD, INT_MAX_FIELD, INT_AVG_FIELD,
      FLOAT_SUM_FIELD, FLOAT_MIN_FIELD, FLOAT_MAX_FIELD, FLOAT_AVG_FIELD);
}
