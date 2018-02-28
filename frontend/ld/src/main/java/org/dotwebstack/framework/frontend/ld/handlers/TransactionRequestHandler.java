package org.dotwebstack.framework.frontend.ld.handlers;

import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionHandler;
import org.eclipse.rdf4j.model.Model;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionRequestHandler implements Inflector<Model, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionRequestHandler.class);

  private Transaction transaction;

  public TransactionRequestHandler(@NonNull Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public Response apply(Model transactionModel) {
      // start transaction
      TransactionHandler transactionHandler = new TransactionHandler(transaction, transactionModel);
      transactionHandler.execute();

      return Response.ok().build();
  }

}
