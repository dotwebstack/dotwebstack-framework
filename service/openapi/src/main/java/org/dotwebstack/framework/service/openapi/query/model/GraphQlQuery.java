package org.dotwebstack.framework.service.openapi.query.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class GraphQlQuery {
  private String queryName;

  private Field field;

  @Builder.Default
  private Map<String, String> variables = new HashMap<>();

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("query ")
        .append(queryName);

    if (variables != null && !variables.isEmpty()) {
      sb.append("(");
      String prefix = "";
      for (Map.Entry<String, String> entry : variables.entrySet()) {
        sb.append(prefix);
        sb.append("$")
            .append(entry.getKey())
            .append(": ");
        sb.append(entry.getValue());
        prefix = ", ";
      }
      sb.append(")");
    }

    sb.append("{\n");
    field.writeAsString(sb, 1);
    sb.append("}");
    return sb.toString();
  }
}
