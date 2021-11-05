package org.dotwebstack.framework.service.openapi.query.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.GraphQlValueWriter;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Field {
  private String name;

  @Builder.Default
  private Map<String, Object> arguments = new HashMap<>();

  private String filterId;

  private String selectionSet;

  @Builder.Default
  private List<Field> children = new ArrayList<>();

  @Builder.Default
  private boolean nodeField = false;

  private boolean collectionNode;

  public void writeAsString(@NonNull StringBuilder sb, int depth) {
    indent(sb, depth);
    sb.append(name);
    boolean hasArguments = arguments != null && !arguments.isEmpty();
    boolean hasFilter = filterId != null;

    if (hasArguments || hasFilter) {
      List<String> serializedArguments = new ArrayList<>();
      if (hasArguments) {
        for (Map.Entry<String, Object> e : arguments.entrySet()) {
          StringBuilder argBuilder = new StringBuilder();
          GraphQlValueWriter.write(e.getValue(), argBuilder);
          serializedArguments.add(String.format("%s: %s", e.getKey(), argBuilder));
        }
      }
      if (hasFilter) {
        serializedArguments.add(String.format("filter: $%s", filterId));
      }
      sb.append(String.format("(%s)", String.join(", ", serializedArguments)));
    }

    if (selectionSet != null) {
      sb.append(selectionSet);
    } else if (children != null && !children.isEmpty()) {
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
