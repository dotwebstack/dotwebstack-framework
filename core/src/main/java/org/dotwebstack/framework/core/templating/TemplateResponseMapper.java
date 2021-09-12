package org.dotwebstack.framework.core.templating;

import java.util.Map;
import reactor.core.publisher.Mono;

public interface TemplateResponseMapper {
  Mono<String> toResponse(String templateName, Map<String, Object> queryInputParams, Object queryResultData,
      Map<String, String> environmentVariables);
}
