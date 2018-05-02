package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import java.util.Map;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionHandler;
import org.dotwebstack.framework.transaction.TransactionHandlerFactory;
import org.dotwebstack.framework.transaction.flow.step.StepFailureException;
import org.eclipse.rdf4j.model.Model;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransactionRequestHandler
    implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionRequestHandler.class);

  private final ApiOperation apiOperation;

  @Getter(AccessLevel.PACKAGE)
  private final Transaction transaction;

  private final TransactionRequestParameterMapper requestParameterMapper;

  private final TransactionRequestBodyMapper transactionRequestBodyMapper;

  private final Swagger swagger;

  private final ApiRequestValidator apiRequestValidator;

  private final TransactionHandlerFactory transactionHandlerFactory;

  TransactionRequestHandler(@NonNull ApiOperation apiOperation, @NonNull Transaction transaction,
      @NonNull TransactionRequestParameterMapper requestParameterMapper,
      @NonNull TransactionRequestBodyMapper transactionRequestBodyMapper,
      @NonNull ApiRequestValidator apiRequestValidator, @NonNull Swagger swagger,
      @NonNull TransactionHandlerFactory transactionHandlerFactory) {
    this.apiRequestValidator = apiRequestValidator;
    this.apiOperation = apiOperation;
    this.transaction = transaction;
    this.requestParameterMapper = requestParameterMapper;
    this.transactionRequestBodyMapper = transactionRequestBodyMapper;
    this.swagger = swagger;
    this.transactionHandlerFactory = transactionHandlerFactory;
  }

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

    Model transactionModel = transactionRequestBodyMapper.map(operation, requestParameters);
    LOG.debug("Transaction model after rml mapping: {}", transactionModel);

    TransactionHandler transactionHandler =
        transactionHandlerFactory.newTransactionHandler(transaction, transactionModel);
    try {
      transactionHandler.execute(parameterValues);
    } catch (StepFailureException stepFailureException) {
      throw new BadRequestException(stepFailureException.getMessage());
    }

    return Response.ok().build();
  }

}
