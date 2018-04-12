package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.service.Service;

@org.springframework.stereotype.Service
public class ServiceRequestHandlerFactory {

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private final EndpointRequestParameterMappers endpointRequestParameterMappers;

  public ServiceRequestHandlerFactory(
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull EndpointRequestParameterMappers endpointRequestParameterMappers) {
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
    this.endpointRequestParameterMappers = endpointRequestParameterMappers;
  }

  public ServiceRequestHandler newServiceRequestHandler(@NonNull Service service) {
    return new ServiceRequestHandler(service, supportedReaderMediaTypesScanner,
        endpointRequestParameterMappers);
  }

}
