package org.dotwebstack.framework.core.helpers;

import java.util.regex.Pattern;

public class StringHelper {
  private static final Pattern NAME_PATTERN_1ST = Pattern.compile("([^A-Z])([0-9]*[A-Z])");

  private static final Pattern NAME_PATTERN_2ND = Pattern.compile("([A-Z])([A-Z][^A-Z])");

  private static final String NAME_REPLACEMENT = "$1_$2";

  private StringHelper() {}

  /**
   * Converts a String into snakeCase.
   *
   * <pre>
   * CaseUtils.toSnakeCase(null)          = null
   * CaseUtils.toSnakeCase("ToSnakeCase") = "to_snake_case"
   * CaseUtils.toSnakeCase("ToSnakeCASE") = "to_snake_case"
   * </pre>
   *
   * @param str - the String to be converted to snakeCase, may be null
   * @return snakeCase of String, {@code null} if null String input
   */
  public static String toSnakeCase(String str) {
    if (str == null) {
      return null;
    }
    var tempName = NAME_PATTERN_1ST.matcher(str)
        .replaceAll(NAME_REPLACEMENT);

    return NAME_PATTERN_2ND.matcher(tempName)
        .replaceAll(NAME_REPLACEMENT)
        .toLowerCase();
  }
}
