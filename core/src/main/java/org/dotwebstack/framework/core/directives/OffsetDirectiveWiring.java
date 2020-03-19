package org.dotwebstack.framework.core.directives;

import org.springframework.stereotype.Component;

@Component
public class OffsetDirectiveWiring extends PagingDirectiveWiring {
  @Override
  public String getDirectiveName() {
    return CoreDirectives.OFFSET_NAME;
  }
}
