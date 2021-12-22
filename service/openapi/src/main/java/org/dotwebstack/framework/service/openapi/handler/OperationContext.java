package org.dotwebstack.framework.service.openapi.handler;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;
import org.springframework.http.HttpStatus;

@Builder
@Getter
public class OperationContext {

  @NonNull
  private final Operation operation;

  @NonNull
  private final Map.Entry<HttpStatus, ApiResponse> responseEntry;

  private final QueryProperties queryProperties;

  public HttpStatus getHttpStatus() {
    return responseEntry.getKey();
  }

  public ApiResponse getResponse() {
    return responseEntry.getValue();
  }

  public boolean isResponseWithBody() {
    return !getHttpStatus().is3xxRedirection() && !getHttpStatus().equals(HttpStatus.NO_CONTENT);
  }

  public boolean hasQuery() {
    return queryProperties != null;
  }
}
