package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.service.Service;
import org.dotwebstack.framework.transaction.TransactionHandlerFactory;

@org.springframework.stereotype.Service
public class ServiceRequestHandlerFactory {

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private final EndpointRequestParameterMapper endpointRequestParameterMapper;

  private final TransactionHandlerFactory transactionHandlerFactory;

  public ServiceRequestHandlerFactory(
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull EndpointRequestParameterMapper endpointRequestParameterMapper,
      @NonNull TransactionHandlerFactory transactionHandlerFactory) {
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
    this.endpointRequestParameterMapper = endpointRequestParameterMapper;
    this.transactionHandlerFactory = transactionHandlerFactory;
  }

  public ServiceRequestHandler newServiceRequestHandler(@NonNull Service service) {
    return new ServiceRequestHandler(service, supportedReaderMediaTypesScanner,
        endpointRequestParameterMapper, transactionHandlerFactory);
  }

}
