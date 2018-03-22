package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionRequestHandlerFactory {

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private final RepresentationRequestParameterMapper representationRequestParameterMapper;

  @Autowired
  public TransactionRequestHandlerFactory(
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull RepresentationRequestParameterMapper representationRequestParameterMapper) {
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
    this.representationRequestParameterMapper = representationRequestParameterMapper;
  }

  public TransactionRequestHandler newTransactionRequestHandler(
      @NonNull Transaction transaction) {
    return new TransactionRequestHandler(transaction, supportedReaderMediaTypesScanner,
        representationRequestParameterMapper);
  }

}
