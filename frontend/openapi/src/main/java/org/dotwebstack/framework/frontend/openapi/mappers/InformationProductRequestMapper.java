package org.dotwebstack.framework.frontend.openapi.mappers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Set;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InformationProductRequestMapper extends AbstractRequestMapper<InformationProduct> {

  @Autowired
  public InformationProductRequestMapper(
      @NonNull InformationProductResourceProvider informationProductLoader,
      @NonNull RequestHandlerFactory requestHandlerFactory) {
    super(requestHandlerFactory, informationProductLoader);
    supported = OpenApiSpecificationExtensions.INFORMATION_PRODUCT;
  }

  @Override
  public void map(Resource.Builder resourceBuilder, OpenAPI openApi, ApiOperation apiOperation,
                  String absolutePath) {
    Operation operation = apiOperation.getOperation();
    validate200Response(operation, absolutePath);

    String okStatusCode = Integer.toString(Status.OK.getStatusCode());
    Set<String> produces =
        operation.getResponses() != null
            ? operation.getResponses().get(okStatusCode).getContent().keySet()
            : null;

    if (produces == null) {
      throw new ConfigurationException(
          String.format("Path '%s' should produce at least one media type.", absolutePath));
    }

    ApiResponse response = operation.getResponses().get(okStatusCode);

    Inflector<ContainerRequestContext, Response> requestHandler =
        requestHandlerFactory.newRequestHandler(apiOperation, getResourceFor(operation), openApi,
            response);

    ResourceMethod.Builder methodBuilder =
        getMethodBuilder(resourceBuilder, apiOperation, requestHandler);

    produces.forEach(methodBuilder::produces);
  }

}
