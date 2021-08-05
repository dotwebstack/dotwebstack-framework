package org.dotwebstack.framework.service.openapi.query.model;

import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.service.openapi.query.filter.ValueWriter;

@Builder
@Data
public class FilterOperatorClause {
  private String operator;

  private Object value;

  public void writeAsString(StringBuilder sb) {
    sb.append(operator);
    sb.append(": ");
    ValueWriter.write(value, sb);
  }
}
