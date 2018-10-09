package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.Transaction;
import org.springframework.stereotype.Service;

@Service
class TransactionRequestParameterMapper extends AbstractRequestParameterMapper {

  Map<String, String> map(@NonNull Operation operation, @NonNull Transaction transaction,
      @NonNull RequestParameters requestParameters) {
    Map<String, String> result = new HashMap<>();

    List<Parameter> parameters = operation.getParameters();
    if (parameters != null) {
      parameters.stream() //
          .map(openApiParameter -> //
          getOtherParameters(transaction.getParameters(), requestParameters, openApiParameter)) //
          .forEach(result::putAll); //
    }
    return result;
  }

}

