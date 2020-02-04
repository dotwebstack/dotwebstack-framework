package org.dotwebstack.framework.service.openapi.requestbody;

import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.web.reactive.function.server.ServerRequest;

public interface RequestBodyHandler {

  Map<String, Object> getValues(@NonNull ServerRequest request, @NonNull RequestBodyContext requestBodyContext,
      @NonNull RequestBody requestBody, Map<String, Object> parameterMap) throws BadRequestException;

  void validate(@NonNull GraphQlField graphQlField, @NonNull RequestBody requestBody, @NonNull String pathName);

  boolean supports(@NonNull RequestBody requestBody);
}
