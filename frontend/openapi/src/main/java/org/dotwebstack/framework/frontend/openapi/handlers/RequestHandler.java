package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.BaseUriFactory;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.Rdf4jUtils;
import org.dotwebstack.framework.frontend.openapi.entity.Entity;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(RequestHandler.class);

  private final ApiOperation apiOperation;

  @Getter(AccessLevel.PACKAGE)
  private final InformationProduct informationProduct;

  @Getter(AccessLevel.PACKAGE)
  private final Map<MediaType, Property> schemaMap;

  private final RequestParameterMapper requestParameterMapper;

  private final Swagger swagger;

  private final ApiRequestValidator apiRequestValidator;

  RequestHandler(@NonNull ApiOperation apiOperation, @NonNull InformationProduct informationProduct,
      @NonNull Map<MediaType, Property> schemaMap,
      @NonNull RequestParameterMapper requestParameterMapper,
      @NonNull ApiRequestValidator apiRequestValidator, @NonNull Swagger swagger) {
    this.apiRequestValidator = apiRequestValidator;
    this.apiOperation = apiOperation;
    this.informationProduct = informationProduct;
    this.schemaMap = schemaMap;
    this.requestParameterMapper = requestParameterMapper;
    this.swagger = swagger;
  }

  /**
   * @throws NotFoundException If the requested resource cannot be found.
   * @throws ConfigurationException If the {@link OpenApiSpecificationExtensions#RESULT_FOUND_QUERY}
   *         vendor extension has been defined, but a 404 response is missing (and vice versa).
   */
  @Override
  public Response apply(@NonNull ContainerRequestContext context) {
    UriInfo uriInfo = context.getUriInfo();
    String path = uriInfo.getPath();

    LOG.debug("Handling {} request for path {}", context.getMethod(), path);

    context.setProperty(RequestHandlerProperties.OPERATION, apiOperation.getOperation());

    RequestParameters requestParameters =
        apiRequestValidator.validate(apiOperation, swagger, context);

    Map<String, String> parameterValues = requestParameterMapper.map(apiOperation.getOperation(),
        informationProduct, requestParameters);

    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      TupleQueryResult result = (TupleQueryResult) informationProduct.getResult(parameterValues);
      TupleEntity entity =
          TupleEntity.builder().withQueryResult(result).withSchemaMap(schemaMap).build();

      return responseOk(entity);
    }

    if (ResultType.GRAPH.equals(informationProduct.getResultType())) {
      String baseUri = BaseUriFactory.newBaseUri((ContainerRequest) context, swagger);

      GraphQueryResult result = (GraphQueryResult) informationProduct.getResult(parameterValues);
      GraphEntity entity = GraphEntity.newGraphEntity(schemaMap, result, swagger, parameterValues,
          informationProduct, baseUri);

      String query = getResultFoundQuery();

      if (query != null && !Rdf4jUtils.evaluateAskQuery(entity.getRepository(), query)) {
        throw new NotFoundException();
      }

      return responseOk(entity);
    }

    LOG.error("Result type {} not supported for information product {}",
        informationProduct.getResultType(), informationProduct.getIdentifier());

    return Response.serverError().build();
  }

  private String getResultFoundQuery() {
    String query = (String) apiOperation.getOperation().getVendorExtensions().get(
        OpenApiSpecificationExtensions.RESULT_FOUND_QUERY);
    String statusCode = String.valueOf(Status.NOT_FOUND.getStatusCode());

    io.swagger.models.Response response404 =
        apiOperation.getOperation().getResponses().get(statusCode);

    if (query != null && response404 == null) {
      throw new ConfigurationException(
          String.format("Vendor extension '%s' has been defined, while %s response is missing",
              OpenApiSpecificationExtensions.RESULT_FOUND_QUERY, statusCode));
    }
    if (query == null && response404 != null) {
      throw new ConfigurationException(
          String.format("Vendor extension '%s' is missing, while %s response has been defined",
              OpenApiSpecificationExtensions.RESULT_FOUND_QUERY, statusCode));
    }

    return query;
  }

  private Response responseOk(Entity entity) {
    if (entity != null) {
      return Response.ok(entity).build();
    }

    return null;
  }

}
