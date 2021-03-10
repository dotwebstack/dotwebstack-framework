package org.dotwebstack.framework.core.datafetchers.aggregate;

import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import org.dotwebstack.framework.core.helpers.TypeHelper;

public class AggregateHelper {

  public static boolean isAggregate(SelectedField selectedField) {
    if (selectedField == null) {
      return false;
    }

    GraphQLObjectType objectType = selectedField.getObjectType();

    return AggregateConstants.AGGREGATE_TYPE.equals(TypeHelper.getTypeName(objectType));
  }
}
