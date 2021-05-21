package org.dotwebstack.framework.core.datafetchers.filter;

import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.DATE_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.DATE_TIME_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.FLOAT_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.INT_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.STRING_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.scalars.CoreScalars.DATE;
import static org.dotwebstack.framework.core.scalars.CoreScalars.DATETIME;

import graphql.Scalars;
import org.dotwebstack.framework.core.config.TypeConfiguration;

public final class FilterHelper {

  private FilterHelper() {}

  public static String getFilterNameForType(TypeConfiguration<?> typeConfiguration, String field) {
    String type = typeConfiguration.getFields()
        .get(field)
        .getType();

    return getFilterNameForType(type);
  }

  public static String getFilterNameForType(String typeName) {
    if (GraphQLString.getName()
        .equals(typeName)) {
      return STRING_FILTER_INPUT_OBJECT_TYPE;
    } else if (GraphQLInt.getName()
        .equals(typeName)) {
      return INT_FILTER_INPUT_OBJECT_TYPE;
    } else if (GraphQLFloat.getName()
        .equals(typeName)) {
      return FLOAT_FILTER_INPUT_OBJECT_TYPE;
    } else if (DATE.getName()
        .equals(typeName)) {
      return DATE_FILTER_INPUT_OBJECT_TYPE;
    } else if (DATETIME.getName()
        .equals(typeName)) {
      return DATE_TIME_FILTER_INPUT_OBJECT_TYPE;
    } else if (Scalars.GraphQLBoolean.getName()
        .equals(typeName)) {
      return typeName;
    }
    throw illegalArgumentException("Type name '{}' has no corresponding filter.", typeName);
  }
}
