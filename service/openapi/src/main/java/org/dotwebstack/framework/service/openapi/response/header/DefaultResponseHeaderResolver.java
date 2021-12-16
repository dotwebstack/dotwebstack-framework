package org.dotwebstack.framework.service.openapi.response.header;

import static java.util.stream.Collectors.toMap;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.jexl.JexlHelper.getJexlContext;

import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper;
import org.dotwebstack.framework.service.openapi.jexl.JexlUtils;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

public class DefaultResponseHeaderResolver implements ResponseHeaderResolver {

  private final OperationRequest operationRequest;

  private final JexlHelper jexlHelper;

  private final JexlContext jexlContext;

  public DefaultResponseHeaderResolver(@NonNull OperationRequest operationRequest, Object data,
      @NonNull EnvironmentProperties environmentProperties, @NonNull JexlEngine jexlEngine) {
    this.operationRequest = operationRequest;
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.jexlContext = getJexlContext(environmentProperties.getAllProperties(), operationRequest.getParameters(), data);
  }

  @Override
  public void accept(HttpHeaders httpHeaders) {
    var headers = operationRequest.getContext()
        .getSuccessResponse()
        .getHeaders();

    if (headers != null) {
      var dwsHeaders = headers.entrySet()
          .stream()
          .map(this::evaluateResponseHeader)
          .filter(entry -> !entry.getValue()
              .isEmpty())
          .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

      httpHeaders.addAll(CollectionUtils.toMultiValueMap(dwsHeaders));
    }
  }

  private Map.Entry<String, List<String>> evaluateResponseHeader(Map.Entry<String, Header> headerEntry) {
    var headerName = headerEntry.getKey();
    var headerSchema = headerEntry.getValue()
        .getSchema();

    if (headerSchema instanceof ArraySchema || headerSchema instanceof ObjectSchema) {
      throw invalidConfigurationException(
          "Unsupported header configuration for `{}`. Headers should have a scalar schema type", headerName);
    }

    var defaultValue = headerSchema.getDefault();

    String headerValue = DwsExtensionHelper.getJexlExpression(headerSchema)
        .map(jexlExpression -> JexlUtils.evaluateJexlExpression(jexlExpression, jexlHelper, jexlContext, Object.class)
            .or(() -> Optional.ofNullable(defaultValue))
            .map(String::valueOf)
            .orElseThrow(() -> invalidConfigurationException(
                "Could not determine value for header `{}` with expression or default value.", headerName)))
        .orElseGet(() -> defaultValue != null ? String.valueOf(defaultValue) : null);

    return headerValue != null ? Map.entry(headerName, List.of(headerValue)) : Map.entry(headerName, List.of());
  }
}
