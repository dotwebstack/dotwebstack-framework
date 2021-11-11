package org.dotwebstack.framework.service.openapi.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Operation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

@Data
public class QueryProperties {

  private String field;

  private Paging paging;

  private List<Filter> filters = new ArrayList<>();

  public static QueryProperties fromOperation(Operation operation) {
    var extension = operation.getExtensions()
        .get(OasConstants.X_DWS_QUERY);

    if (extension instanceof String) {
      var properties = new QueryProperties();
      properties.setField((String) extension);
      return properties;
    }

    return new ObjectMapper().convertValue(extension, QueryProperties.class);
  }

  @Data
  public static class Paging {

    private String pageSize;

    private String page;
  }

  @Data
  public static class Filter {

    private List<String> fieldPath;

    private String type;

    private Map<?, ?> fieldFilters;
  }
}
