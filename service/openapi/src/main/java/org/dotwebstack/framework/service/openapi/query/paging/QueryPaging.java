package org.dotwebstack.framework.service.openapi.query.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_FIELD_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.parameterValidationException;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryPaging {

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

      if (!(pageSizeValue instanceof Integer) || !(pageValue instanceof Integer)) {
        throw invalidConfigurationException("`page` and `pageSize` parameters must be configured having type integer.");
      }

      int pageSize = (Integer) pageSizeValue;
      int page = (Integer) pageValue;

      if (pageSize < 1) {
        throw parameterValidationException("`pageSize` parameter value should be 1 or higher, but was {}.", pageSize);
      }

      pagingArguments.put(FIRST_ARGUMENT_NAME, pageSize);
      pagingArguments.put(OFFSET_FIELD_NAME, getOffsetValue(page, pageSize));
    }

    return pagingArguments;
  }

  private static int getOffsetValue(int page, int pageSize) {
    int offset = 0;

    if (page < 1) {
      throw parameterValidationException("`page` parameter value should be 1 or higher, but was {}.", page);
    }

    if (page > 1) {
      offset = (page - 1) * pageSize;
    }

    return offset;
  }
}
