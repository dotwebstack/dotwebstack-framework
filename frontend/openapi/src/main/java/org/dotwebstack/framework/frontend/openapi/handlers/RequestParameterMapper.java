package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductUtils;
import org.dotwebstack.framework.param.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class RequestParameterMapper {

  private static final Logger LOG = LoggerFactory.getLogger(RequestParameterMapper.class);

  Map<String, String> map(@NonNull Operation operation, @NonNull InformationProduct product,
      @NonNull RequestParameters requestParameters) {
    Map<String, String> result = new HashMap<>();

    for (io.swagger.models.parameters.Parameter openApiParameter : operation.getParameters()) {

      if (openApiParameter instanceof BodyParameter) {
        Collection<Property> properties =
            ((BodyParameter) openApiParameter).getSchema().getProperties().values();
        for (Property property : properties) {
          Map<String, Object> vendorExtensions = property.getVendorExtensions();

          LOG.debug("Vendor extensions for property '{}' in parameter '{}': {}", property.getName(),
              openApiParameter.getName(), vendorExtensions);

          Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

          if (parameterIdString == null) {
            // Vendor extension x-dotwebstack-parameter not found for property
            continue;
          }

          Parameter<?> parameter =
              InformationProductUtils.getParameter(product, (String) parameterIdString);

          String value = requestParameters.get(parameter.getName());

          result.put(parameter.getName(), value);
        }
      } else {
        Map<String, Object> vendorExtensions = openApiParameter.getVendorExtensions();

        LOG.debug("Vendor extensions for parameter '{}': {}", openApiParameter.getName(),
            vendorExtensions);

        Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

        if (parameterIdString == null) {
          // Vendor extension x-dotwebstack-parameter not found for property
          continue;
        }

        Parameter<?> parameter =
            InformationProductUtils.getParameter(product, (String) parameterIdString);

        String value = requestParameters.get(parameter.getName());

        result.put(parameter.getName(), value);
      }
    }
    return result;
  }
}

