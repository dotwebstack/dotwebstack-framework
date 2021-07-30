package org.dotwebstack.framework.service.openapi.query;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Field {
  private String name;

  private Map<String, Object> arguments;

  private List<Field> children;

  public void toString(StringBuilder sb, int depth) {
    indent(sb, depth);
    sb.append(name);
    if (arguments != null && !arguments.isEmpty()) {
      StringJoiner stringJoiner = new StringJoiner(sb);
      sb.append("(");
      arguments.forEach((key, value) -> stringJoiner.add(key + ": \"" + value.toString() + "\""));
      sb.append(stringJoiner);
      sb.append(")");
    }
    if (children != null && !children.isEmpty()) {
      sb.append(" {\n");
      children.forEach(c -> c.toString(sb, depth + 1));
      indent(sb, depth);
      sb.append("}");
    }
    sb.append("\n");
  }

  protected void indent(StringBuilder sb, int depth) {
    sb.append(" ".repeat(Math.max(0, depth)));
  }
}
