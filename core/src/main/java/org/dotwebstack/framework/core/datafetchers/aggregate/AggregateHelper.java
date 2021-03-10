package org.dotwebstack.framework.core.datafetchers.aggregate;

import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.helpers.TypeHelper;

public class AggregateHelper {

  public static boolean isAggregateField(SelectedField selectedField) {
    if (selectedField == null) {
      return false;
    }

    GraphQLObjectType objectType = selectedField.getObjectType();

    return AggregateConstants.AGGREGATE_TYPE.equals(TypeHelper.getTypeName(objectType));
  }

  public static boolean isAggregate(FieldConfiguration fieldConfiguration) {
    return !StringUtils.isEmpty(fieldConfiguration.getAggregationOf());
  }
}
