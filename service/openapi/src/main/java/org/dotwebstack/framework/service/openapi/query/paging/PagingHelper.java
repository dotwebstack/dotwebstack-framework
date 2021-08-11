package org.dotwebstack.framework.service.openapi.query.paging;

import static org.dotwebstack.framework.service.openapi.query.FieldHelper.resolveField;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.dwssettings.QueryPaging;

public class PagingHelper {

  public static void addPaging(@NonNull GraphQlQuery query, @NonNull List<QueryPaging> paging,
      @NonNull Map<String, Object> inputParams) {

    paging.forEach(p -> {
      String[] path = p.getPath();
      Field targetField = resolveField(query, path);

      String offsetString = (String) inputParams.get(p.getOffsetParam()
          .split("\\.")[1]);
      String firstString = (String) inputParams.get(p.getFirstParam()
          .split("\\.")[1]);

      if (offsetString != null) {
        targetField.getArguments()
            .put("offset", Integer.parseInt(offsetString));
      }
      if (firstString != null) {
        targetField.getArguments()
            .put("first", Integer.parseInt(firstString));
      }
    });
  }
}
