package org.dotwebstack.framework.service.openapi.response;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.query.GraphQlField;

@Builder
@Getter
public class ResponseWriteContext {

  GraphQlField graphQlField;

  ResponseObject responseObject;

  Object data;

  @Builder.Default
  Deque<FieldContext> dataStack = new ArrayDeque<>();

  Map<String, Object> parameters;

  URI uri;

  public boolean isSchemaRequiredNonNillable() {
    return getResponseObject().getSummary()
        .isRequired()
        && !getResponseObject().getSummary()
            .isNillable();
  }

  public boolean isSchemaRequiredNillable() {
    return getResponseObject().getSummary()
        .isRequired()
        && getResponseObject().getSummary()
            .isNillable();
  }
}
