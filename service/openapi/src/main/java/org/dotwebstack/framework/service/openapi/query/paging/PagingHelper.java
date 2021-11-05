package org.dotwebstack.framework.service.openapi.query.paging;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

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
        first = Integer.parseInt(pageSizeString);
        query.getField()
            .getArguments()
            .put("first", first);
      } else {
        // TODO badRequest
        throw illegalArgumentException("pageSize not provided");
      }

      if (pageString != null) {
        query.getField()
            .getArguments()
            .put("offset", getOffsetValue(pageString, first));
      } else {
        // TODO badRequest
        throw illegalArgumentException("page not provided");
      }
    }
  }

  private static int getOffsetValue(String pageString, int first) {
    int page = Integer.parseInt(pageString);
    int offset = 0;

    if (page < 1) {
      // TODO badRequest
      throw illegalArgumentException("TODo");
    }

    if (page > 1) {
      offset = (page - 1) * first;
    }

    return offset;
  }
}
