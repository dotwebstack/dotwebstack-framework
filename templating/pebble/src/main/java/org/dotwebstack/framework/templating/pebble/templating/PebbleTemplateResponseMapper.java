package org.dotwebstack.framework.templating.pebble.templating;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.templating.TemplateResponseMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PebbleTemplateResponseMapper implements TemplateResponseMapper {

  private Map<String, PebbleTemplate> htmlTemplates;

  private static final String INPUT_PARAMS_PREFIX = "args";

  private static final String QUERY_RESULTS_PREFIX = "fields";

  private static final String ENV_VARS_PREFIX = "env";

  public PebbleTemplateResponseMapper(Map<String, PebbleTemplate> htmlTemplates) {
    this.htmlTemplates = htmlTemplates;
  }

  @Override
  public String toResponse(String templateName, Map<String, Object> queryInputParams,
      Map<String, Object> queryResultData, Map<String, String> environmentVariables) {

    if (!htmlTemplates.containsKey(templateName)) {
      throw invalidConfigurationException("Template with name {} does not exist", templateName);
    }

    PebbleTemplate template = htmlTemplates.get(templateName);
    Writer writer = new StringWriter();
    try {
      template.evaluate(writer, resolveMapsToOne(queryInputParams, queryResultData, environmentVariables));
    } catch (IOException exception) {
      throw new TemplateEvaluationException("Could not evaluate template " + templateName, exception);
    }

    return writer.toString();
  }

  private Map<String, Object> resolveMapsToOne(Map<String, Object> queryInputParams,
      Map<String, Object> queryResultData, Map<String, String> environmentVariables) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<>();

    result.put(INPUT_PARAMS_PREFIX, queryInputParams);
    result.put(QUERY_RESULTS_PREFIX, queryResultData);
    result.put(ENV_VARS_PREFIX, environmentVariables);

    return result;
  }

}
