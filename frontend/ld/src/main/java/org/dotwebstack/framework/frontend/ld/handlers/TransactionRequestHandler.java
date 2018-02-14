package org.dotwebstack.framework.frontend.ld.handlers;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.Transaction;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionRequestHandler.class);

  private Transaction transaction;

  public TransactionRequestHandler(@NonNull Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();
    LOG.debug("Handling POST request for path {}", path);

    // start transaction

    return Response.ok().build();

  }

}
