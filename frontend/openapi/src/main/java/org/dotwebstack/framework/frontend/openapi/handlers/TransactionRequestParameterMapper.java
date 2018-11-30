package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.Transaction;
import org.springframework.stereotype.Service;

@Service
class TransactionRequestParameterMapper {

  private RequestParameterMapperHelper helper;

  public TransactionRequestParameterMapper(RequestParameterMapperHelper helper) {
    this.helper = helper;
  }

  Map<String, String> map(@NonNull Operation operation, @NonNull Transaction transaction,
      @NonNull RequestParameters requestParameters) {
    RequestBody requestBody = null;
    Map<String, String> result =
        helper.mapParametersToRequest(operation, requestParameters, requestBody,
            transaction.getParameters());
    return result;

  }

}

