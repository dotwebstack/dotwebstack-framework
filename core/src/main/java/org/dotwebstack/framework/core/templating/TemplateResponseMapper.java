package org.dotwebstack.framework.core.templating;

import java.util.Map;

public interface TemplateResponseMapper {
  String toResponse(String templateName, Map<String, Object> queryInputParams, Object queryResultData,
      Map<String, String> environmentVariables);
}
