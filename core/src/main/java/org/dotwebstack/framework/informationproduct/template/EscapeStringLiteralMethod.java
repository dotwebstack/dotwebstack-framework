package org.dotwebstack.framework.informationproduct.template;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

public class EscapeStringLiteralMethod implements TemplateMethodModelEx {

  /* the way this method gets exposed to FreeMarker context */
  static final String CTX_NAME = "literal";
  private static final String[] in = {"\t", "\n", "\r", "\b", "\f", "\"", "'", "\\"};
  private static final String[] out = {"\\t", "\\n", "\\r", "\\b", "\\f", "\\\"", "\\'", "\\\\"};

  @Override
  public Object exec(@NonNull List arguments) throws TemplateModelException {
    if (arguments.isEmpty()) {
      throw new TemplateModelException(
          "No arguments provided. Expected first argument of type String");
    }

    return StringUtils.replaceEach(((SimpleScalar) arguments.get(0)).getAsString(), in, out);
  }

}
