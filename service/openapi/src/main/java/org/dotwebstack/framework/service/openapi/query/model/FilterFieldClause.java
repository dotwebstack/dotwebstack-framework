package org.dotwebstack.framework.service.openapi.query.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class FilterFieldClause {
  private String fieldName;

  private boolean negate;

  @Builder.Default
  private List<FilterOperatorClause> clauses = new ArrayList<>();

  public void writeAsString(StringBuilder sb) {
    if (negate) {
      sb.append("not: {");
      writeField(sb);
      sb.append("}");
    } else {
      writeField(sb);
    }
  }

  private void writeField(StringBuilder sb) {
    sb.append(fieldName)
        .append(": { ");
    String prefix = "";
    for (FilterOperatorClause clause : clauses) {
      sb.append(prefix);
      clause.writeAsString(sb);
      prefix = ", ";
    }
    sb.append("}");
  }
}
