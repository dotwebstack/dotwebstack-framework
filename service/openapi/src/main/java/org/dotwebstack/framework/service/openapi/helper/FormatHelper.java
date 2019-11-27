package org.dotwebstack.framework.service.openapi.helper;

import lombok.NonNull;

public class FormatHelper {

  private static final String INDENTCHAR = "\t";

  private FormatHelper() {}

  public static String formatGraphQlQuery(@NonNull String query) {
    int indents = 0;
    StringBuilder builder = new StringBuilder();
    for (String character : query.split("")) {
      switch (character) {
        case "{":
          builder.append(" {\n");
          indents++;
          builder.append(INDENTCHAR.repeat(Math.max(0, indents)));
          break;
        case "}":
          builder.append("\n");
          indents--;
          builder.append(INDENTCHAR.repeat(Math.max(0, indents)));
          builder.append("}");
          break;
        case ",":
          builder.append(",\n");
          builder.append(INDENTCHAR.repeat(Math.max(0, indents)));
          break;
        default:
          builder.append(character);
          break;
      }
    }

    return builder.toString();
  }
}
