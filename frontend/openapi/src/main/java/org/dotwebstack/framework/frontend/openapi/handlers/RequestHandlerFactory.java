package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.SwaggerUtils;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionHandlerFactory;
import org.springframework.stereotype.Service;

@Service
public class RequestHandlerFactory {

  private final InformationProductRequestParameterMapper informationProductRequestParameterMapper;

  private final TransactionRequestParameterMapper transactionRequestParameterMapper;
  
  private final TransactionRequestBodyMapper transactionRequestBodyMapper;

  private final RequestParameterExtractor requestParameterExtractor;

  private final TransactionHandlerFactory transactionHandlerFactory;

  public RequestHandlerFactory(
      @NonNull InformationProductRequestParameterMapper informationProductRequestParameterMapper,
      @NonNull TransactionRequestParameterMapper transactionRequestParameterMapper,
      @NonNull TransactionRequestBodyMapper transactionRequestBodyMapper,
      @NonNull RequestParameterExtractor requestParameterExtractor,
      @NonNull TransactionHandlerFactory transactionHandlerFactory) {
    this.informationProductRequestParameterMapper = informationProductRequestParameterMapper;
    this.transactionRequestParameterMapper = transactionRequestParameterMapper;
    this.transactionRequestBodyMapper = transactionRequestBodyMapper;
    this.requestParameterExtractor = requestParameterExtractor;
    this.transactionHandlerFactory = transactionHandlerFactory;
  }

  public InformationProductRequestHandler newInformationProductRequestHandler(
      @NonNull ApiOperation apiOperation,
      @NonNull InformationProduct informationProduct, @NonNull Response response,
      @NonNull Swagger swagger) {
    return new InformationProductRequestHandler(apiOperation, informationProduct, response,
        informationProductRequestParameterMapper,
        new ApiRequestValidator(SwaggerUtils.createValidator(swagger), requestParameterExtractor),
        swagger);
  }

  public TransactionRequestHandler newTransactionRequestHandler(@NonNull ApiOperation apiOperation,
      @NonNull Transaction transaction, @NonNull Swagger swagger) {
    return new TransactionRequestHandler(apiOperation, transaction,
        transactionRequestParameterMapper, transactionRequestBodyMapper,
        new ApiRequestValidator(SwaggerUtils.createValidator(swagger), requestParameterExtractor),
        swagger, transactionHandlerFactory);
  }

}
