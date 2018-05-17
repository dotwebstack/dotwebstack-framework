package org.dotwebstack.framework.frontend.ld.handlers;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.service.Service;
import org.dotwebstack.framework.transaction.TransactionHandler;
import org.dotwebstack.framework.transaction.TransactionHandlerFactory;
import org.dotwebstack.framework.transaction.flow.step.StepFailureException;
import org.dotwebstack.framework.validation.ShaclValidationException;
import org.eclipse.rdf4j.model.Model;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceRequestHandler.class);

  private final Service service;

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private final EndpointRequestParameterMapper endpointRequestParameterMapper;

  private final TransactionHandlerFactory transactionHandlerFactory;

  public ServiceRequestHandler(@NonNull Service service,
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner,
      @NonNull EndpointRequestParameterMapper endpointRequestParameterMapper,
      @NonNull TransactionHandlerFactory transactionHandlerFactory) {
    this.service = service;
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
    this.endpointRequestParameterMapper = endpointRequestParameterMapper;
    this.transactionHandlerFactory = transactionHandlerFactory;
  }

  @Override
  public Response apply(@NonNull ContainerRequestContext containerRequestContext) {
    Model transactionModel = null;
    MediaType mediaType =
        new MediaType(CONTENT_TYPE, containerRequestContext.getHeaderString(CONTENT_TYPE));

    for (MessageBodyReader<Model> reader : supportedReaderMediaTypesScanner.getModelReaders()) {
      if (reader.isReadable(Model.class, null, null, mediaType)
          && reader.getClass().getAnnotation(Consumes.class).value()[0].equals(
              mediaType.getSubtype())) {
        try {
          transactionModel = reader.readFrom(Model.class, null, null, mediaType, null,
              containerRequestContext.getEntityStream());
        } catch (Exception e) {
          return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
      }
    }

    if (transactionModel == null) {
      return Response.status(Status.NOT_ACCEPTABLE).entity(
          String.format("Content type %s not supported", mediaType.toString())).build();
    }

    Map<String, String> parameterValues = new HashMap<>();
    endpointRequestParameterMapper.map(service.getTransaction(), containerRequestContext).forEach(
        parameterValues::put);

    service.getParameterMappers().forEach(
        parameterMapper -> parameterValues.putAll(parameterMapper.map(containerRequestContext)));

    TransactionHandler transactionHandler =
        transactionHandlerFactory.newTransactionHandler(service.getTransaction(), transactionModel);
    try {
      transactionHandler.execute(parameterValues);
    } catch (StepFailureException | ShaclValidationException e) {
      LOG.error("Got this error {} during executing of the transaction handler", e);
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (RuntimeException e) {
      LOG.error("Got this error {} during executing of the transaction handler", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    return Response.ok().build();
  }

}
