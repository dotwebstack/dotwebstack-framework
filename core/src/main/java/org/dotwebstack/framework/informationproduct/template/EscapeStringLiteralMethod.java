package org.dotwebstack.framework.informationproduct.template;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * Escapes the following characters within a string literal:
 * <ul>
 * <li>\t</li>
 * <li>\t</li>
 * <li>\r</li>
 * <li>\b</li>
 * <li>\f</li>
 * <li>"</li>
 * <li>'</li>
 * <li>\</li>
 * </ul>
 * This method can be used by invoking the {@code literal()} function within a Freemarker template.
 */
public class EscapeStringLiteralMethod implements TemplateMethodModelEx {

  /* the way this method gets exposed to FreeMarker context */
  static final String CTX_NAME = "literal";
  private static final String[] in = {"\t", "\n", "\r", "\b", "\f", "\"", "'", "\\"};
  private static final String[] out = {"\\t", "\\n", "\\r", "\\b", "\\f", "\\\"", "\\'", "\\\\"};

  /**
   * @throws TemplateModelException If no argument is provided. Expected is a {@code List} with
   *         exactly one element of type {@code String}.
   */
  @Override
  public Object exec(@NonNull List arguments) throws TemplateModelException {
    if (arguments.isEmpty()) {
      throw new TemplateModelException(
          "No arguments provided. Expected first argument of type String");
    }

    return StringUtils.replaceEach(((SimpleScalar) arguments.get(0)).getAsString(), in, out);
  }

}
