package org.dotwebstack.framework.informationproduct.template;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class EscapeLiteralMethod implements TemplateMethodModelEx {

  /* the way this method gets exposed to FreeMarker context */
  static final String CTX_NAME = "literal";

  private static final Map<String, String> SPARQL_ESCAPE_SEARCH_REPLACEMENTS = new HashMap<>();

  static {
    SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\t", "\\t");
    SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\n", "\\n");
    SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\r", "\\r");
    SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\b", "\\b");
    SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\f", "\\f");
    SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\"", "\\\"");
    SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("'", "\\'");
    SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\\", "\\\\");
  }

  @Override
  public Object exec(@NonNull List arguments) throws TemplateModelException {
    if (arguments.isEmpty()) {
      throw new TemplateModelException(
          "No arguments provided. Expected first argument of type String");
    }

    return escape(((SimpleScalar) arguments.get(0)).getAsString());
  }

  private static String escape(String string) {

    StringBuilder builder = new StringBuilder(string);
    for (int i = 0; i < builder.length(); i++) {
      String replacement = SPARQL_ESCAPE_SEARCH_REPLACEMENTS.get("" + builder.charAt(i));
      if (replacement != null) {
        builder.deleteCharAt(i);
        builder.insert(i, replacement);
        // advance past the replacement
        i += (replacement.length() - 1);
      }
    }
    return builder.toString();
  }
}
