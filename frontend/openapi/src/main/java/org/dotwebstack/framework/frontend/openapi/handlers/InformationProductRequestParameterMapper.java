package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.springframework.stereotype.Service;

@Service
class InformationProductRequestParameterMapper extends AbstractRequestParameterMapper {

  Map<String, String> map(@NonNull Operation operation, @NonNull InformationProduct product,
      @NonNull RequestParameters requestParameters) {
    Map<String, String> result = new HashMap<>();

    RequestBody requestBody = operation.getRequestBody();
    if (requestBody != null) {
      result.putAll(getBodyParameters(product.getParameters(), requestParameters, requestBody));
    }

    if (operation.getParameters() != null) {
      operation.getParameters().stream() //
          .map(openApiParameter ->  //
              getOtherParameters(product.getParameters(), requestParameters, openApiParameter)) //
          .forEach(result::putAll); //
    }
    return result;
  }

}

