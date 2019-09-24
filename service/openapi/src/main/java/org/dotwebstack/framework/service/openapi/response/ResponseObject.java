package org.dotwebstack.framework.service.openapi.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseObject {

  private String identifier;

  private ResponseSchema schema;

}
