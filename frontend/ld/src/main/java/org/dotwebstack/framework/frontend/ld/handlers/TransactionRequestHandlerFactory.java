package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.transaction.Transaction;
import org.springframework.stereotype.Service;

@Service
public class TransactionRequestHandlerFactory {

  public TransactionRequestHandler newTransactionRequestHandler(
      @NonNull Transaction transaction) {
    return new TransactionRequestHandler(transaction);
  }

}
