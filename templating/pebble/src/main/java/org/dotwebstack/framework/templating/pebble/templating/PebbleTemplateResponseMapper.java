package org.dotwebstack.framework.templating.pebble.templating;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.templating.TemplateResponseMapper;
import org.springframework.stereotype.Component;

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
    if (!htmlTemplates.containsKey(templateName)) {
      throw new InvalidConfigurationException("Template with name {} does not exist", templateName);
    }

    PebbleTemplate template = htmlTemplates.get(templateName);
    Writer writer = new StringWriter();
    try {
      template.evaluate(writer, (Map<String, Object>) input);
    } catch (IOException exception) {
      throw new TemplateEvaluationException("Could not evaluate template " + templateName, exception);
    }

    return writer.toString();
  }
}
