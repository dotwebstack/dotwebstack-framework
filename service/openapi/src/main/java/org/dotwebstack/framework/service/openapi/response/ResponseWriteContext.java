package org.dotwebstack.framework.service.openapi.response;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.service.openapi.response.oas.OasField;

@Builder
@Getter
public class ResponseWriteContext {

  String identifier;

  OasField oasField;

  Object data;

  @Builder.Default
  Deque<FieldContext> dataStack = new ArrayDeque<>();

  @Builder.Default
  Map<String, Object> parameters = new HashMap<>();

  URI uri;

  public boolean isSchemaRequiredNonNillable() {
    return oasField.isRequired() && !oasField.isNillable();
  }

  public boolean isSchemaRequiredNillable() {
    return oasField.isRequired() && oasField.isNillable();
  }
}
