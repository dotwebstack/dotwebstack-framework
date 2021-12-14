package org.dotwebstack.framework.service.openapi.response.header;

import lombok.NonNull;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.springframework.stereotype.Component;

@Component
public class ResponseHeaderResolverFactory {

  private final EnvironmentProperties environmentProperties;

  private final JexlEngine jexlEngine;

  public ResponseHeaderResolverFactory(@NonNull EnvironmentProperties environmentProperties,
      @NonNull JexlEngine jexlEngine) {
    this.environmentProperties = environmentProperties;
    this.jexlEngine = jexlEngine;
  }

  public ResponseHeaderResolver create(OperationRequest operationRequest) {
    return new DefaultResponseHeaderResolver(operationRequest, environmentProperties, jexlEngine);
  }
}
