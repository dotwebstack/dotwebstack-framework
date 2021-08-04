package org.dotwebstack.framework.service.openapi.query.filter;


import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Filter {

  @Builder.Default
  private List<FilterClause> filterClauses = new ArrayList<>();

  public void writeAsString(StringBuilder sb) {
    sb.append("filter: {");
    String prefix = "";
    for (FilterClause clause : filterClauses) {
      sb.append(prefix);
      clause.writeAsString(sb);
      prefix = ", ";
    }
    sb.append("}");
  }
}
