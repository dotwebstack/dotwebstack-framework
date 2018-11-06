package org.dotwebstack.framework.frontend.openapi.mappers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import java.util.Set;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionResourceProvider;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionRequestMapper extends AbstractRequestMapper<Transaction> {

  @Autowired
  public TransactionRequestMapper(
      @NonNull TransactionResourceProvider transactionResourceProvider,
      @NonNull RequestHandlerFactory requestHandlerFactory) {
    super(requestHandlerFactory, transactionResourceProvider);
    supported = OpenApiSpecificationExtensions.TRANSACTION;
  }

  @Override
  public void map(Resource.Builder resourceBuilder, OpenAPI openApi, ApiOperation apiOperation,
      Operation operation, String absolutePath) {
    validate200Response(operation, absolutePath);

    Set<String> consumes =
        operation.getRequestBody() != null ? operation.getRequestBody().getContent().keySet()
            : null;

    if (consumes == null) {
      throw new ConfigurationException(
          String.format("Path '%s' should consume at least one media type.", absolutePath));
    }

    Inflector<ContainerRequestContext, Response> requestHandler =
        requestHandlerFactory.newRequestHandler(apiOperation, getResourceFor(operation), openApi);

    ResourceMethod.Builder methodBuilder =
        getMethodBuilder(resourceBuilder, apiOperation, requestHandler);

    consumes.forEach(methodBuilder::consumes);
  }

}
