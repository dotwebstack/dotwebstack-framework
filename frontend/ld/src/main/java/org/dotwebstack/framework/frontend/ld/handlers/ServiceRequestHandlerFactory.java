package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.service.Service;

@org.springframework.stereotype.Service
public class ServiceRequestHandlerFactory {

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private final EndpointRequestParameterMapper endpointRequestParameterMapper;

  public ServiceRequestHandlerFactory(
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull EndpointRequestParameterMapper endpointRequestParameterMapper) {
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
    this.endpointRequestParameterMapper = endpointRequestParameterMapper;
  }

  public ServiceRequestHandler newServiceRequestHandler(@NonNull Service service) {
    return new ServiceRequestHandler(service, supportedReaderMediaTypesScanner,
        endpointRequestParameterMapper);
  }

}
