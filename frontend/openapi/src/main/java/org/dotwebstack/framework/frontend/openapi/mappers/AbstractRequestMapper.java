package org.dotwebstack.framework.frontend.openapi.mappers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.v3.oas.models.Operation;
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.ResourceProvider;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

@Slf4j
public abstract class AbstractRequestMapper<R> implements RequestMapper {

  private final ResourceProvider<R> resourceProvider;

  final RequestHandlerFactory requestHandlerFactory;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();
  protected String supported;

  AbstractRequestMapper(RequestHandlerFactory requestHandlerFactory,
      @NonNull ResourceProvider<R> transactionResourceProvider) {
    this.requestHandlerFactory = requestHandlerFactory;
    this.resourceProvider = transactionResourceProvider;
  }

  @Override
  public Boolean supportsVendorExtension(@Nullable Map<String, Object> vendorExtensions) {
    return vendorExtensions != null
        && vendorExtensions.keySet().stream().anyMatch(
            key -> key.equals(supported));
  }

  void validate200Response(Operation operation, String absolutePath) {
    String okStatusCode = Integer.toString(Response.Status.OK.getStatusCode());

    if (operation.getResponses() == null
        || !operation.getResponses().containsKey(okStatusCode)) {
      throw new ConfigurationException(String.format(
          "Resource '%s' does not specify a status %s response.", absolutePath, okStatusCode));
    }
  }

  R getResourceFor(Operation getOperation) {
    IRI identifier = valueFactory.createIRI((String) getOperation.getExtensions().get(supported));
    return resourceProvider.get(identifier);
  }


  ResourceMethod.Builder getMethodBuilder(Resource.Builder resourceBuilder,
      ApiOperation apiOperation, Inflector<ContainerRequestContext, Response> requestHandler) {
    return resourceBuilder.addMethod(apiOperation.getMethod().name()).handledBy(requestHandler);
  }

}
