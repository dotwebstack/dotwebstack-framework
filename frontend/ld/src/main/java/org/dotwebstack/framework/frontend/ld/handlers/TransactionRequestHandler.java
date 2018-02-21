package org.dotwebstack.framework.frontend.ld.handlers;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionHandler;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
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

    // body to model
    if (containerRequestContext.getMediaType().toString().contains(MediaTypes.RDFXML)) {

      RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);

      Model transactionModel = new LinkedHashModel();
      rdfParser.setRDFHandler(new StatementCollector(transactionModel));

      try {
        rdfParser.parse(containerRequestContext.getEntityStream(), "");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      // start transaction
      TransactionHandler transactionHandler = new TransactionHandler(transaction, transactionModel);
      transactionHandler.execute();

      return Response.ok().build();
    }

    throw new ConfigurationException(
        String.format("Media type %s not supported for transaction %s",
            containerRequestContext.getMediaType().toString(), transaction.getIdentifier()));
  }

}
