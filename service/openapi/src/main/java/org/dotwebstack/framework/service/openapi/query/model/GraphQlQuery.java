package org.dotwebstack.framework.service.openapi.query.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GraphQlQuery {
  private String queryName;

  private Field field;

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("query ")
        .append(queryName)
        .append("{\n");
    field.writeAsString(sb, 1);
    sb.append("}");
    return sb.toString();
  }
}
