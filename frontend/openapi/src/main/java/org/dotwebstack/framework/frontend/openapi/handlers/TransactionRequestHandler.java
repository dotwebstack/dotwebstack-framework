package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionHandler;
import org.dotwebstack.framework.transaction.flow.step.StepFailureException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransactionRequestHandler
    implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionRequestHandler.class);

  private final ApiOperation apiOperation;

  @Getter(AccessLevel.PACKAGE)
  private final Transaction transaction;

  @Getter(AccessLevel.PACKAGE)
  private final io.swagger.models.Response response;

  private final TransactionRequestParameterMapper requestParameterMapper;

  private final TransactionBodyMapper transactionBodyMapper;

  private final Swagger swagger;

  private final ApiRequestValidator apiRequestValidator;

  TransactionRequestHandler(@NonNull ApiOperation apiOperation, @NonNull Transaction transaction,
      @NonNull io.swagger.models.Response response,
      @NonNull TransactionRequestParameterMapper requestParameterMapper,
      @NonNull TransactionBodyMapper transactionBodyMapper,
      @NonNull ApiRequestValidator apiRequestValidator, @NonNull Swagger swagger) {
    this.apiRequestValidator = apiRequestValidator;
    this.apiOperation = apiOperation;
    this.transaction = transaction;
    this.response = response;
    this.requestParameterMapper = requestParameterMapper;
    this.transactionBodyMapper = transactionBodyMapper;
    this.swagger = swagger;
  }

  /**
   * @throws NotFoundException If the requested resource cannot be found.
   */
  @Override
  public Response apply(@NonNull ContainerRequestContext context) {
    UriInfo uriInfo = context.getUriInfo();
    String path = uriInfo.getPath();

    LOG.debug("Handling {} request for path {}", context.getMethod(), path);

    Operation operation = apiOperation.getOperation();
    context.setProperty(RequestHandlerProperties.OPERATION, operation);

    RequestParameters requestParameters =
        apiRequestValidator.validate(apiOperation, swagger, context);

    Map<String, String> parameterValues =
        requestParameterMapper.map(operation, transaction, requestParameters);

    Model transactionModel = transactionBodyMapper.map(operation, transaction, requestParameters);

    TransactionHandler transactionHandler = new TransactionHandler(
        new SailRepository(new MemoryStore()), transaction, transactionModel);
    try {
      transactionHandler.execute(parameterValues);
    } catch (StepFailureException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (RuntimeException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    return Response.ok().build();
  }

}
