package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.ParameterUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
abstract class AbstractRequestParameterMapper {

  protected Map<String, String> getBodyParameters(@NonNull Collection<Parameter> parameters,
      @NonNull RequestParameters requestParameters, RequestBody requestBody) {
    Map<String, String> result = new HashMap<>();

    Content content = defaultIfNull(requestBody.getContent(), new Content());

    Schema schema = defaultIfNull(content.get(
        MediaType.APPLICATION_JSON.toString()).getSchema(),
        new ObjectSchema().properties(ImmutableMap.of()));
    Map<String, Schema> requestBodyPropertyMap = schema.getProperties();
    Collection<Schema> properties = requestBodyPropertyMap.values();

    for (Schema property : properties) {
      Map<String, Object> vendorExtensions =
          defaultIfNull(property.getExtensions(), ImmutableMap.of());

      LOG.debug("Vendor extensions for property '{}' in parameter '{}': {}", property.getName(),
          requestBody.getDescription(), vendorExtensions);

      Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

      if (parameterIdString == null) {
        // Vendor extension x-dotwebstack-parameter not found for property
        continue;
      }

      Parameter<?> parameter =
          ParameterUtils.getParameter(parameters, (String) parameterIdString);

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

