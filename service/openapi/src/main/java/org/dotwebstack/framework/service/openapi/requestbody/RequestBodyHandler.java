package org.dotwebstack.framework.service.openapi.requestbody;

import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public interface RequestBodyHandler {

  Mono<Map<String, Object>> getValues(@NonNull ServerRequest request, @NonNull RequestBodyContext requestBodyContext,
      @NonNull RequestBody requestBody, Map<String, Object> parameterMap);

  void validate(@NonNull GraphQlField graphQlField, @NonNull RequestBody requestBody, @NonNull String pathName);

  boolean supports(@NonNull RequestBody requestBody);
}
