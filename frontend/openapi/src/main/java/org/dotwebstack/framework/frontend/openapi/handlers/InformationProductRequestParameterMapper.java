package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
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

    for (Parameter openApiParameter : operation.getParameters()) {

      if (openApiParameter instanceof BodyParameter) {
        result.putAll(getBodyParameters(product.getParameters(), requestParameters,
            (BodyParameter) openApiParameter));
      } else {
        result.putAll(
            getOtherParameters(product.getParameters(), requestParameters, openApiParameter));
      }
    }
    return result;
  }

}

