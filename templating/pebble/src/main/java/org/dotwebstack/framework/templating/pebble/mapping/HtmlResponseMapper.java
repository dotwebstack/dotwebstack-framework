package org.dotwebstack.framework.templating.pebble.mapping;

import com.mitchellbosecke.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
public class HtmlResponseMapper implements ResponseMapper {

  private static final String HTML_MIMETYPE = "text/html";

  private Map<String, PebbleTemplate> htmlTemplates;

  public HtmlResponseMapper(Map<String, PebbleTemplate> htmlTemplates) {
    this.htmlTemplates = htmlTemplates;
  }

  @Override
  public boolean supportsOutputMimeType(MimeType mimeType) {
    return MimeType.valueOf(HTML_MIMETYPE)
        .equals(mimeType);
  }

  @Override
  public boolean supportsInputObjectClass(Class<?> clazz) {
    return Map.class.isAssignableFrom(clazz);
  }

  @Override
  public String toResponse(Object input) {
    Writer writer = new StringWriter();
    if (input instanceof LinkedHashMap) {
      htmlTemplates.forEach((s, pebbleTemplate) -> {
        try {
          pebbleTemplate.evaluate(writer, (LinkedHashMap) input);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }

    return writer.toString();
  }
}
