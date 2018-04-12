package org.dotwebstack.framework.frontend.openapi.mappers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import java.util.List;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.OptionsRequestHandler;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionResourceProvider;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionRequestMapper implements RequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionRequestMapper.class);

  private final TransactionResourceProvider transactionResourceProvider;

  private final RequestHandlerFactory requestHandlerFactory;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Autowired
  public TransactionRequestMapper(
      @NonNull TransactionResourceProvider transactionResourceProvider,
      @NonNull RequestHandlerFactory requestHandlerFactory) {
    this.transactionResourceProvider = transactionResourceProvider;
    this.requestHandlerFactory = requestHandlerFactory;
  }

  public Boolean supportsVendorExtension(String key) {
    return (key.equals(OpenApiSpecificationExtensions.TRANSACTION));
  }

  public Resource map(Swagger swagger, Path pathItem, ApiOperation apiOperation,
      Operation getOperation, String absolutePath) {
    String okStatusCode = Integer.toString(Status.OK.getStatusCode());

    if (getOperation.getResponses() == null
        || !getOperation.getResponses().containsKey(okStatusCode)) {
      throw new ConfigurationException(String.format(
          "Resource '%s' does not specify a status %s response.", absolutePath, okStatusCode));
    }

    List<String> consumes =
        getOperation.getConsumes() != null ? getOperation.getConsumes() : swagger.getConsumes();

    if (consumes == null) {
      throw new ConfigurationException(
          String.format("Path '%s' should consume at least one media type.", absolutePath));
    }

    Response response = getOperation.getResponses().get(okStatusCode);

    IRI transactionIdentifier =
        valueFactory.createIRI((String) getOperation.getVendorExtensions().get(
            OpenApiSpecificationExtensions.TRANSACTION));

    Transaction transaction =
        transactionResourceProvider.get(transactionIdentifier);

    Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);

    ResourceMethod.Builder methodBuilder =
        resourceBuilder.addMethod(apiOperation.getMethod().name()).handledBy(
            requestHandlerFactory.newTransactionRequestHandler(apiOperation, transaction, response,
                swagger));

    consumes.forEach(methodBuilder::consumes);

    resourceBuilder.addMethod(HttpMethod.OPTIONS).handledBy(new OptionsRequestHandler(pathItem));

    LOG.debug("Mapped {} operation for request path {}", apiOperation.getMethod().name(),
        absolutePath);

    return resourceBuilder.build();
  }

}
