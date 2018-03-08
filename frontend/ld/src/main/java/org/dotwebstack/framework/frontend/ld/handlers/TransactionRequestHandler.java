package org.dotwebstack.framework.frontend.ld.handlers;

import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionHandler;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.glassfish.jersey.process.Inflector;

public class TransactionRequestHandler implements Inflector<Model, Response> {

  private Transaction transaction;

  public TransactionRequestHandler(@NonNull Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public Response apply(@NonNull Model transactionModel) {
    TransactionHandler transactionHandler = new TransactionHandler(
        new SailRepository(new MemoryStore()), transaction, transactionModel);
    transactionHandler.execute();

    return Response.ok().build();
  }

}
