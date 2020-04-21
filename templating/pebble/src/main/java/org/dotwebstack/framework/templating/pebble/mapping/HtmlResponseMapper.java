package org.dotwebstack.framework.templating.pebble.mapping;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

@Component
public class HtmlResponseMapper implements ResponseMapper {

  private static final String HTML_MIMETYPE = "text/html";

  private Map<String, PebbleTemplate> templates;

  public HtmlResponseMapper(Map<String, PebbleTemplate> getTemplates) {
    templates = getTemplates;
  }

  @Override
  public boolean supportsOutputMimeType(MimeType mimeType) {
    boolean result = MimeType.valueOf(HTML_MIMETYPE).equals(mimeType);
    return result;
  }

  @Override
  public boolean supportsInputObjectClass(Class<?> clazz) {
    boolean result = Map.class.isAssignableFrom(clazz);
    return result;
  }

  @Override
  public String toResponse(Object input) {

    System.out.println("test");
//
//    Writer writer = new StringWriter();
//    compiledTemplate.evaluate(writer, context);
//
//    String output = writer.toString();

    return "";
  }
}
