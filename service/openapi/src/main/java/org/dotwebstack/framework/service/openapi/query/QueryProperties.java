package org.dotwebstack.framework.service.openapi.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Operation;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

@Data
public class QueryProperties {

  private String field;

  private Paging paging;

  private Map<String, String> keys = new HashMap<>();

  private Map<String, Map<String, Object>> filters = new HashMap<>();

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
}
