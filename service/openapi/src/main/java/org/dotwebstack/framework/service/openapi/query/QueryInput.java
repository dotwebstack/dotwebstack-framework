package org.dotwebstack.framework.service.openapi.query;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryInput {

  private String query;

  private Map<String, Object> variables;

}
