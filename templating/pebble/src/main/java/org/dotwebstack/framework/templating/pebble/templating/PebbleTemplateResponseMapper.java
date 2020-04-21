package org.dotwebstack.framework.templating.pebble.templating;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.templating.TemplateResponseMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

@Component
@Slf4j
public class PebbleTemplateResponseMapper implements TemplateResponseMapper {

  private Map<String, PebbleTemplate> htmlTemplates;

  public PebbleTemplateResponseMapper(Map<String, PebbleTemplate> htmlTemplates) {
    this.htmlTemplates = htmlTemplates;
  }

  @SuppressWarnings("unchecked")
  @Override
  public String toResponse(String templateName, Object input) {
    Writer writer = new StringWriter();

    if (input instanceof Map) {
      PebbleTemplate template = htmlTemplates.get(templateName);
      try {
        template.evaluate(writer, (Map<String, Object>) input);
      } catch (IOException exception) {
        LOG.error("IOException for Pebble evaluate", exception);
      }

    }

    String result = writer.toString();
    System.out.println(result);
    return result;
  }
}
