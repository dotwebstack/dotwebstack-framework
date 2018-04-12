package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.SwaggerUtils;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.transaction.Transaction;
import org.springframework.stereotype.Service;

@Service
public class RequestHandlerFactory {

  private final InformationProductRequestParameterMapper informationProductRequestParameterMapper;

  private final TransactionRequestParameterMapper transactionRequestParameterMapper;
  
  private final TransactionBodyMapper transactionBodyMapper;

  private final RequestParameterExtractor requestParameterExtractor;


  public RequestHandlerFactory(
      @NonNull InformationProductRequestParameterMapper informationProductRequestParameterMapper,
      @NonNull TransactionRequestParameterMapper transactionRequestParameterMapper,
      @NonNull TransactionBodyMapper transactionBodyMapper,
      @NonNull RequestParameterExtractor requestParameterExtractor) {
    this.informationProductRequestParameterMapper = informationProductRequestParameterMapper;
    this.transactionRequestParameterMapper = transactionRequestParameterMapper;
    this.transactionBodyMapper = transactionBodyMapper;
    this.requestParameterExtractor = requestParameterExtractor;
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
      @NonNull Transaction transaction, @NonNull Response response,
      @NonNull Swagger swagger) {
    return new TransactionRequestHandler(apiOperation, transaction, response, 
        transactionRequestParameterMapper, transactionBodyMapper,
        new ApiRequestValidator(SwaggerUtils.createValidator(swagger), requestParameterExtractor),
        swagger);
  }


}
