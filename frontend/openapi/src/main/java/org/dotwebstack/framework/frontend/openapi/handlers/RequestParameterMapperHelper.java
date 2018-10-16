package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.ParameterUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class RequestParameterMapperHelper {

  public Map<String, String> mapParametersToRequest(Operation operation,
      RequestParameters requestParameters, RequestBody requestBody,
      Collection<org.dotwebstack.framework.param.Parameter> parameters) {
    Map<String, String> result = new HashMap<>();
    if (requestBody != null) {
      result.putAll(
          this.getBodyParameters(parameters, requestParameters, requestBody));
    }

    List<io.swagger.v3.oas.models.parameters.Parameter> operationParameters =
        operation.getParameters();
    if (operationParameters != null) {
      operationParameters.stream() //
          .map(openApiParameter -> //
          this.getOtherParameters(parameters, requestParameters, openApiParameter)) //
          .forEach(result::putAll); //
    }
    return result;
  }

  public Map<String, String> getOtherParameters(Collection<Parameter> parameters,
      @NonNull RequestParameters requestParameters,
      io.swagger.v3.oas.models.parameters.Parameter openApiParameter) {

    String param = getParameterOfExtensions(openApiParameter.getExtensions(), parameters);
    if (param == null) {
      return Collections.emptyMap();
    }
    return ImmutableMap.of(param, requestParameters.get(param));
  }

  public Map<String, String> getBodyParameters(@NonNull Collection<Parameter> parameters,
      @NonNull RequestParameters requestParameters, RequestBody requestBody) {

    Collection<Schema> values = getRequestBodyPropertySchemas(requestBody);
    List<String> schemaParameters = getParameterNamesFromSchemas(parameters, values);

    return schemaParameters.stream().collect(
        Collectors.toMap(param -> param, requestParameters::get));
  }

  private List<String> getParameterNamesFromSchemas(Collection<Parameter> parameters,
      Collection<Schema> values) {
    return values.stream()//
        .map(schema -> schema.getExtensions())//
        .map(extension -> getParameterOfExtensions(extension, parameters))//
        .filter(Objects::nonNull)//
        .collect(Collectors.toList());
  }

  private Collection<Schema> getRequestBodyPropertySchemas(RequestBody requestBody) {
    Content content = requestBody.getContent();
    if (content == null) {
      return Collections.emptyList();
    }
    io.swagger.v3.oas.models.media.MediaType mediaType =
        content.get(MediaType.APPLICATION_JSON.toString());
    if (mediaType == null) {
      return Collections.emptyList();
    }
    Schema<?> schema = mediaType.getSchema();
    if (schema == null) {
      return Collections.emptyList();
    }
    Map<String, Schema> properties = schema.getProperties();
    if (properties == null) {
      return Collections.emptyList();
    }
    return properties.values();
  }



  private String getParameterOfExtensions(Map<String, Object> extension,
      Collection<Parameter> parameters) {
    if (extension == null) {
      return null;
    }
    String extensionParameter = (String) extension.get(OpenApiSpecificationExtensions.PARAMETER);
    if (extensionParameter == null) {
      return null;
    }
    return ParameterUtils.getParameter(parameters, extensionParameter).getName();
  }

}

