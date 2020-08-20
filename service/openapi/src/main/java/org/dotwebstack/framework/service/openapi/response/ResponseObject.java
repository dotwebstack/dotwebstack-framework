package org.dotwebstack.framework.service.openapi.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
public class ResponseObject {

  private String identifier;

  private ResponseObject parent;

  @Setter
  private SchemaSummary summary;

  public boolean isComposedOf() {
    return !getSummary().getComposedOf()
        .isEmpty();
  }

}
