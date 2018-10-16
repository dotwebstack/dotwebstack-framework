package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.springframework.stereotype.Service;

@Service
class InformationProductRequestParameterMapper {

  private RequestParameterMapperHelper helper;

  public InformationProductRequestParameterMapper(RequestParameterMapperHelper helper) {
    this.helper = helper;
  }

  Map<String, String> map(@NonNull Operation operation, @NonNull InformationProduct product,
      @NonNull RequestParameters requestParameters) {

    RequestBody requestBody = operation.getRequestBody();
    Map<String, String> result =
        helper.mapParametersToRequest(operation, requestParameters, requestBody,
            product.getParameters());
    return result;
  }



}

