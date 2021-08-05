package org.dotwebstack.framework.service.openapi.query.model;


import static org.dotwebstack.framework.service.openapi.query.filter.ValueWriter.write;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphQlFilter {

  private Map<?, ?> content;

  public void writeAsString(StringBuilder sb) {
    sb.append("filter: ");
    write(content, sb);
  }
}
