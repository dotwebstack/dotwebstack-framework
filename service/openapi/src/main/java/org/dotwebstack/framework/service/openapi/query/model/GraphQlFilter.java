package org.dotwebstack.framework.service.openapi.query.model;

import static org.dotwebstack.framework.service.openapi.query.GraphQlValueWriter.write;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphQlFilter {

  private Map<?, ?> content;

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("filter: ");
    write(content, sb);
    return sb.toString();
  }
}
