package org.dotwebstack.framework.core.templating;

import org.springframework.stereotype.Component;

@Component
public interface TemplateResponseMapper {
  String toResponse(String templateName, Object input);
}
