package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class TransactionRequestParameterMapper {

  private static final Logger LOG =
      LoggerFactory.getLogger(TransactionRequestParameterMapper.class);

  Map<String, String> map(@NonNull Operation operation, @NonNull Transaction transaction,
      @NonNull RequestParameters requestParameters) {
    Map<String, String> result = new HashMap<>();

    for (io.swagger.models.parameters.Parameter openApiParameter : operation.getParameters()) {
      if (!(openApiParameter instanceof BodyParameter)) {
        Map<String, Object> vendorExtensions = openApiParameter.getVendorExtensions();

        LOG.debug("Vendor extensions for parameter '{}': {}", openApiParameter.getName(),
            vendorExtensions);

        Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

        if (parameterIdString == null) {
          // Vendor extension x-dotwebstack-parameter not found for property
          continue;
        }

        Parameter<?> parameter =
            TransactionUtils.getParameter(transaction, (String) parameterIdString);

        String value = requestParameters.get(parameter.getName());

        result.put(parameter.getName(), value);
      }
    }
    return result;
  }

}

