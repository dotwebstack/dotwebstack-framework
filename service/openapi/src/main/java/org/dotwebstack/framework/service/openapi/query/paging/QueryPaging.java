package org.dotwebstack.framework.service.openapi.query.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_MAX_VALUE;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_FIELD_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_MAX_VALUE;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.parameterValidationException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryPaging {

  private static final String PAGE_SIZE = "pageSize";

  private static final String PAGE = "page";

  public static Map<String, Integer> toPagingArguments(QueryProperties.Paging paging, Map<String, Object> parameters) {
    Map<String, Integer> pagingArguments = new HashMap<>();
    if (paging != null) {
      var pageSizeValue = parameters.get(paging.getPageSize()
          .split("\\.")[1]);
      var pageValue = parameters.get(paging.getPage()
          .split("\\.")[1]);

      if (pageSizeValue == null || pageValue == null) {
        throw invalidConfigurationException(
            "`page` and `pageSize` parameters are required for paging. Default values should be configured.");
      }

      var pageSize = getIntValueForArgument(pageSizeValue, PAGE_SIZE);

      if (pageSize < 1) {
        throw parameterValidationException("`pageSize` parameter value should be 1 or higher, but was {}.", pageSize);
      }

      if (pageSize > FIRST_MAX_VALUE) {
        throw parameterValidationException("`pageSize` parameter value exceeds allowed value.");
      }

      var page = getIntValueForArgument(pageValue, PAGE);

      var offset = getOffsetValue(page, pageSize);

      if (offset > OFFSET_MAX_VALUE) {
        throw parameterValidationException("`page` parameter value exceeds allowed value.");
      }

      pagingArguments.put(FIRST_ARGUMENT_NAME, pageSize);
      pagingArguments.put(OFFSET_FIELD_NAME, offset);
    }

    return pagingArguments;
  }

  private static int getIntValueForArgument(Object value, String name) {
    if (value instanceof Integer) {
      return (Integer) value;
    } else if (value instanceof BigInteger) {
      return ((BigInteger) value).intValue();
    } else {
      throw invalidConfigurationException("`{}` parameter must be configured having type integer.", name);
    }
  }

  private static int getOffsetValue(int page, int pageSize) {
    var offset = 0;

    if (page < 1) {
      throw parameterValidationException("`page` parameter value should be 1 or higher, but was {}.", page);
    }

    if (page > 1) {
      offset = (page - 1) * pageSize;
    }

    return offset;
  }
}
