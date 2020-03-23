package org.dotwebstack.framework.core.directives;

import org.springframework.stereotype.Component;

@Component
public class LimitDirectiveWiring extends PagingDirectiveWiring {
  @Override
  public String getDirectiveName() {
    return CoreDirectives.LIMIT_NAME;
  }
}
