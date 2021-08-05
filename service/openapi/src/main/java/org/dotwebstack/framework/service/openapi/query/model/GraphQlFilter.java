package org.dotwebstack.framework.service.openapi.query.model;


import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphQlFilter {

  @Builder.Default
  private List<FilterFieldClause> fieldClauses = new ArrayList<>();

  public void writeAsString(StringBuilder sb) {
    sb.append("filter: {");
    String prefix = "";
    for (FilterFieldClause clause : fieldClauses) {
      sb.append(prefix);
      clause.writeAsString(sb);
      prefix = ", ";
    }
    sb.append("}");
  }
}
