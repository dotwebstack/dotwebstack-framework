package org.dotwebstack.framework.frontend.ld.handlers;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionHandler;
import org.dotwebstack.framework.transaction.flow.step.StepFailureException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.glassfish.jersey.process.Inflector;

public class ServiceRequestHandler implements Inflector<Model, Response> {

  private Transaction transaction;

  public ServiceRequestHandler(@NonNull Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public Response apply(@NonNull Model transactionModel) {
    TransactionHandler transactionHandler = new TransactionHandler(
        new SailRepository(new MemoryStore()), transaction, transactionModel);
    try {
      transactionHandler.execute();
    } catch (StepFailureException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (RuntimeException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    return Response.ok().build();
  }

}
