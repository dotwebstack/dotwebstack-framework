package org.dotwebstack.framework.templating.pebble.mapping;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.springframework.util.MimeType;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class HtmlResponseMapper implements ResponseMapper {

  private static final String HTML_MIMETYPE = "text/html";

  @Override
  public boolean supportsOutputMimeType(MimeType mimeType) {
    return MimeType.valueOf(HTML_MIMETYPE).equals(mimeType);
  }

  @Override
  public boolean supportsInputObjectClass(Class<?> clazz) {
    return false;
  }

  @Override
  public String toResponse(Object input) {
    return null;
  }
}
