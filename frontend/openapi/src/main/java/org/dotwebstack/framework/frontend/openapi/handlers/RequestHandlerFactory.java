package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecUtils;
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

  public InformationProductRequestHandler newRequestHandler(
      @NonNull ApiOperation apiOperation,
      @NonNull InformationProduct informationProduct,
      @NonNull OpenAPI openApi,
      @NonNull ApiResponse response) {
    return new InformationProductRequestHandler(apiOperation,
        informationProduct,
        response,
        informationProductRequestParameterMapper,
        new ApiRequestValidator(OpenApiSpecUtils.createValidator(openApi),
            requestParameterExtractor),
        openApi);
  }

  public TransactionRequestHandler newRequestHandler(@NonNull ApiOperation apiOperation,
      @NonNull Transaction transaction,
      @NonNull OpenAPI openApi) {
    return new TransactionRequestHandler(apiOperation, transaction,
        transactionRequestParameterMapper, transactionRequestBodyMapper,
        new ApiRequestValidator(OpenApiSpecUtils.createValidator(openApi),
            requestParameterExtractor),
        transactionHandlerFactory);
  }

}
