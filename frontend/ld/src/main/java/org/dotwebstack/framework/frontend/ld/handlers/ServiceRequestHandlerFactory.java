package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.service.Service;

@org.springframework.stereotype.Service
public class ServiceRequestHandlerFactory {

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private final EndPointRequestParameterMapper endPointRequestParameterMapper;

  public ServiceRequestHandlerFactory(
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull EndPointRequestParameterMapper endPointRequestParameterMapper) {
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
    this.endPointRequestParameterMapper = endPointRequestParameterMapper;
  }

  public ServiceRequestHandler newServiceRequestHandler(@NonNull Service service) {
    return new ServiceRequestHandler(service, supportedReaderMediaTypesScanner,
        endPointRequestParameterMapper);
  }

}
