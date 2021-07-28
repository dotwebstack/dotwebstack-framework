package org.dotwebstack.framework.service.openapi.query;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResourceQuery {
  private String queryName;

  private Field field;

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("query ")
        .append(queryName)
        .append("{\n");
    field.toString(sb, 1);
    sb.append("}");
    return sb.toString();
  }
}
