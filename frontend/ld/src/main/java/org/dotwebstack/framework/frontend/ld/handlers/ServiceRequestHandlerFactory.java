package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.transaction.Transaction;
import org.springframework.stereotype.Service;

@Service
public class ServiceRequestHandlerFactory {

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private final EndPointRequestParameterMapper endPointRequestParameterMapper;

  public ServiceRequestHandlerFactory(
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull EndPointRequestParameterMapper endPointRequestParameterMapper) {
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
    this.endPointRequestParameterMapper = endPointRequestParameterMapper;
  }

  public ServiceRequestHandler newTransactionRequestHandler(@NonNull Transaction transaction) {
    return new ServiceRequestHandler(transaction, supportedReaderMediaTypesScanner,
        endPointRequestParameterMapper);
  }

}
