package org.dotwebstack.framework.core.helpers;

import lombok.NonNull;

public class FormatHelper {

  private FormatHelper() {}

  public static String formatGraphQlQuery(@NonNull String query) {
    int indents = 0;
    StringBuilder builder = new StringBuilder();
    for (String character : query.split("")) {
      switch (character) {
        case "{":
          builder.append(" {\n");
          indents++;
          builder.append("\t".repeat(Math.max(0, indents)));
          break;
        case "}":
          builder.append("\n");
          indents--;
          builder.append("\t".repeat(Math.max(0, indents)));
          builder.append("}");
          break;
        case ",":
          builder.append(",\n");
          builder.append("\t".repeat(Math.max(0, indents)));
          break;
        default:
          builder.append(character);
          break;
      }
    }

    return builder.toString();
  }

  public static String formatQuery(@NonNull String query) {
    int indents = 0;
    String fixedQuery = query.replaceAll("[ ]*\\{[ ]*", "{")
        .replaceAll("[ ]*}[ ]*", "}");

    StringBuilder builder = new StringBuilder();
    String indent = "  ";
    for (char character : fixedQuery.toCharArray()) {
      switch (character) {
        case '{':
          builder.append("{\n");
          indents++;
          builder.append(indent.repeat(Math.max(0, indents)));
          break;
        case '}':
          builder.append("\n");
          indents--;
          builder.append(indent.repeat(Math.max(0, indents)));
          builder.append("}\n");
          break;
        case ',':
          builder.append(",\n");
          builder.append(indent.repeat(Math.max(0, indents)));
          break;
        case '\n':
          builder.append("\n");
          builder.append(indent.repeat(Math.max(0, indents)));
          break;
        default:
          builder.append(character);
          break;
      }
    }

    return builder.toString();
  }
}
