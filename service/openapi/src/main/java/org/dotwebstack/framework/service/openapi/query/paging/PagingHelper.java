package org.dotwebstack.framework.service.openapi.query.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_FIELD_NAME;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.parameterValidationException;

import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.dwssettings.QueryPaging;

public class PagingHelper {

  private PagingHelper() {}

  public static void addPaging(@NonNull GraphQlQuery query, QueryPaging paging,
      @NonNull Map<String, Object> inputParams) {
    if (paging != null) {
      String pageSizeString = (String) inputParams.get(paging.getPageSizeParam()
          .split("\\.")[1]);
      String pageString = (String) inputParams.get(paging.getPageParam()
          .split("\\.")[1]);

      int first;
      if (pageSizeString != null) {
        try {
          first = Integer.parseInt(pageSizeString);
        } catch (NumberFormatException numberFormatException) {
          throw parameterValidationException("pageSize parameter value should be an integer 1 or higher, but was {}",
              pageSizeString);
        }

        if (first < 1) {
          throw parameterValidationException("pageSize parameter value should be 1 or higher, but was {}", first);
        }

        query.getField()
            .getArguments()
            .put(FIRST_ARGUMENT_NAME, first);
      } else {
        throw parameterValidationException("pageSize parameter value not provided");
      }

      if (pageString != null) {
        query.getField()
            .getArguments()
            .put(OFFSET_FIELD_NAME, getOffsetValue(pageString, first));
      } else {
        throw parameterValidationException("page parameter value not provided");
      }
    }
  }

  private static int getOffsetValue(String pageString, int first) {
    int page;
    try {
      page = Integer.parseInt(pageString);
    } catch (NumberFormatException numberFormatException) {
      throw parameterValidationException("page parameter value should be an integer 1 or higher, but was {}",
          pageString);
    }

    int offset = 0;

    if (page < 1) {
      throw parameterValidationException("page parameter value should be 1 or higher, but was {}", page);
    }

    if (page > 1) {
      offset = (page - 1) * first;
    }

    return offset;
  }
}
