package org.dotwebstack.framework.service.openapi.response;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseWriteContext {

  ResponseObject schema;

  Object data;

  Map<String, Object> parameters;

  public boolean isSchemaRequiredNonNillable() {
    return getSchema().isRequired() && !getSchema().isNillable();
  }
}
