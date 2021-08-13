package org.dotwebstack.framework.service.openapi.query.paging;

import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.dwssettings.QueryPaging;

public class PagingHelper {

  private PagingHelper() {}

  public static void addPaging(@NonNull GraphQlQuery query, QueryPaging paging,
      @NonNull Map<String, Object> inputParams) {
    if (paging != null) {
      String offsetString = (String) inputParams.get(paging.getOffsetParam()
          .split("\\.")[1]);
      String firstString = (String) inputParams.get(paging.getFirstParam()
          .split("\\.")[1]);

      if (offsetString != null) {
        query.getField()
            .getArguments()
            .put("offset", Integer.parseInt(offsetString));
      }
      if (firstString != null) {
        query.getField()
            .getArguments()
            .put("first", Integer.parseInt(firstString));
      }
    }
  }
}
