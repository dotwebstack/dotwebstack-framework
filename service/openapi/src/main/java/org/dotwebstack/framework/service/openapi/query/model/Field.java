package org.dotwebstack.framework.service.openapi.query.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.filter.Filter;
import org.dotwebstack.framework.service.openapi.query.filter.ValueWriter;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Field {
  private String name;

  @Builder.Default
  private Map<String, Object> arguments = new HashMap<>();

  private Filter filter;

  private List<Field> children;

  public void writeAsString(@NonNull StringBuilder sb, int depth) {
    indent(sb, depth);
    sb.append(name);
    if (arguments != null && !arguments.isEmpty()) {
      sb.append("(");
      String prefix = "";
      for (Map.Entry<String, Object> e : arguments.entrySet()) {
        sb.append(prefix);
        sb.append(e.getKey())
            .append(": ");
        ValueWriter.write(e.getValue(), sb);
        prefix = ", ";
      }
      sb.append(")");
    } else if (filter != null) {
      sb.append("(");
      filter.writeAsString(sb);
      sb.append(")");
    }
    if (children != null && !children.isEmpty()) {
      sb.append(" {\n");
      children.forEach(c -> c.writeAsString(sb, depth + 1));
      indent(sb, depth);
      sb.append("}");
    }
    sb.append("\n");
  }

  protected void indent(StringBuilder sb, int depth) {
    sb.append(" ".repeat(Math.max(0, depth)));
  }
}
