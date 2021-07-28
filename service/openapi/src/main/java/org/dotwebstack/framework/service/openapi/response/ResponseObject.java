package org.dotwebstack.framework.service.openapi.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
public class ResponseObject {

  private final String identifier;

  private final ResponseObject parent;

  @Setter
  private SchemaSummary summary;

  public boolean isComposedOf() {
    return !getSummary().getComposedOf()
        .isEmpty();
  }


}
