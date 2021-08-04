package org.dotwebstack.framework.service.openapi.query.filter;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterClause {
  private String operator;

  private String field;

  private Object value;

  @Builder.Default
  private List<FilterClause> clauses = new ArrayList<>(); // mutually exclusive with value

  public void writeAsString(StringBuilder sb) {

    if (field != null) { // name with value
      sb.append(field)
          .append(": {");
      if (!clauses.isEmpty()) {
        writeClauses(sb);
      } else {
        sb.append(operator)
            .append(": ");
        ValueWriter.write(value, sb);
      }
      sb.append("}");
    } else { // operator and subclauses
      sb.append(operator)
          .append(": ");
      writeClauses(sb);
    }
  }

  private void writeClauses(StringBuilder sb) {
    sb.append("{");
    String prefix = "";
    for (FilterClause clause : clauses) {
      sb.append(prefix);
      clause.writeAsString(sb);
      prefix = ", ";
    }
    sb.append("}");
  }
}
