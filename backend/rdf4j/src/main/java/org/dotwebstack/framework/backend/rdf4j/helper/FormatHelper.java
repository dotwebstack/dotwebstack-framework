package org.dotwebstack.framework.backend.rdf4j.helper;

import lombok.NonNull;

public class FormatHelper {

  private FormatHelper() {}

  private static String INDENTCHAR = "  ";

  public static String formatQuery(@NonNull String query) {
    int indents = 0;

    String fixedQuery = query.replaceAll("\n", " ")
        .replaceAll("[ ]+", " ")
        .replaceAll(" \\.", " .\n")
        .replaceAll("[ ]*\\{[ ]*", "{")
        .replaceAll("[ ]*}[ ]*", "}");

    StringBuilder builder = new StringBuilder();
    for (char character : fixedQuery.toCharArray()) {
      switch (character) {
        case '{':
          builder.append("{\n");
          indents++;
          builder.append(INDENTCHAR.repeat(Math.max(0, indents)));
          break;
        case '}':
          builder.append("\n");
          indents--;
          builder.append(INDENTCHAR.repeat(Math.max(0, indents)));
          builder.append("}\n");
          builder.append(INDENTCHAR.repeat(Math.max(0, indents)));
          break;
        case '\n':
          builder.append("\n");
          builder.append(INDENTCHAR.repeat(Math.max(0, indents)));
          break;
        default:
          builder.append(character);
          break;
      }
    }

    return builder.toString()
        .replaceAll("(?m)^\\s*$[\n\r]{1,}", "")
        .replaceAll("(?<=[a-zA-Z0-9])(?=\\{)", " ");
  }
}
