package org.dotwebstack.framework.core.templating;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public interface TemplateResponseMapper {
  String toResponse(String templateName, Map<String, Object> queryInputParams, Map<String, Object> queryResultData,
      Map<String, String> environmentVariables);
}
