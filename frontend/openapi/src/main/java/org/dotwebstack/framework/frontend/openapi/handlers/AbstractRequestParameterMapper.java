package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.ParameterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
abstract class AbstractRequestParameterMapper {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractRequestParameterMapper.class);

  protected Map<String, String> getBodyParameters(@NonNull Collection<Parameter> parameters,
      @NonNull RequestParameters requestParameters, RequestBody requestBody) {
    Map<String, String> result = new HashMap<>();

    Collection<Schema> properties = requestBody.getContent().get(
        MediaType.APPLICATION_JSON.toString()).getSchema().getProperties().values();
    for (Schema property : properties) {
      Map<String, Object> vendorExtensions = property.getExtensions();

      LOG.debug("Vendor extensions for property '{}' in parameter '{}': {}", property.getName(),
          requestBody.getDescription(), vendorExtensions);

      Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

      if (parameterIdString == null) {
        // Vendor extension x-dotwebstack-parameter not found for property
        continue;
      }

      Parameter<?> parameter = ParameterUtils.getParameter(parameters, (String) parameterIdString);

      String value = requestParameters.get(parameter.getName());

      result.put(parameter.getName(), value);
    }

    return result;
  }

  protected Map<String, String> getOtherParameters(Collection<Parameter> parameters,
      @NonNull RequestParameters requestParameters,
      io.swagger.v3.oas.models.parameters.Parameter openApiParameter) {
    Map<String, String> result = new HashMap<>();

    Map<String, Object> vendorExtensions = openApiParameter.getExtensions();

    LOG.debug("Vendor extensions for parameter '{}': {}", openApiParameter.getName(),
        vendorExtensions);

    Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

    if (parameterIdString == null) {
      // Vendor extension x-dotwebstack-parameter not found for property
      return result;
    }

    Parameter<?> parameter = ParameterUtils.getParameter(parameters, (String) parameterIdString);

    String value = requestParameters.get(parameter.getName());

    result.put(parameter.getName(), value);

    return result;
  }

}

