package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
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
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InformationProductRequestHandler
    implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(InformationProductRequestHandler.class);

  private final ApiOperation apiOperation;

  @Getter(AccessLevel.PACKAGE)
  private final InformationProduct informationProduct;

  @Getter(AccessLevel.PACKAGE)
  private final ApiResponse response;

  private final InformationProductRequestParameterMapper requestParameterMapper;

  private final OpenAPI openApi;

  private final ApiRequestValidator apiRequestValidator;

  InformationProductRequestHandler(@NonNull ApiOperation apiOperation,
      @NonNull InformationProduct informationProduct, @NonNull ApiResponse response,
      @NonNull InformationProductRequestParameterMapper requestParameterMapper,
      @NonNull ApiRequestValidator apiRequestValidator, @NonNull OpenAPI openApi) {
    this.apiRequestValidator = apiRequestValidator;
    this.apiOperation = apiOperation;
    this.informationProduct = informationProduct;
    this.response = response;
    this.requestParameterMapper = requestParameterMapper;
    this.openApi = openApi;
  }

  /**
   * @throws NotFoundException If the requested resource cannot be found.
   * @throws ConfigurationException If the {@link OpenApiSpecificationExtensions#SUBJECT_QUERY}
   *         vendor extension is absent, while requesting a graph result.
   * @throws RequestHandlerRuntimeException If the
   *         {@link OpenApiSpecificationExtensions#SUBJECT_QUERY} vendor extension is present, and
   *         the query fails to evaluate.
   */
  @Override
  public Response apply(@NonNull ContainerRequestContext context) {
    UriInfo uriInfo = context.getUriInfo();
    String path = uriInfo.getPath();

    LOG.debug("Handling {} request for path {}", context.getMethod(), path);

    Operation operation = apiOperation.getOperation();
    context.setProperty(RequestHandlerProperties.OPERATION, operation);

    RequestParameters requestParameters =
        apiRequestValidator.validate(apiOperation, context);

    Map<String, String> parameterValues =
        requestParameterMapper.map(operation, informationProduct, requestParameters);

    String baseUri =
        BaseUriFactory.determineBaseUri((ContainerRequest) context, openApi, operation);
    RequestContext requestContext =
        new RequestContext(apiOperation, informationProduct, parameterValues, baseUri);

    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      TupleQueryResult result = (TupleQueryResult) informationProduct.getResult(parameterValues);
      TupleEntity entity =
          TupleEntity.builder().withResult(result).withResponse(response).withRequestContext(
              requestContext).build();

      return responseOk(entity);
    }

    if (ResultType.GRAPH.equals(informationProduct.getResultType())) {
      GraphQueryResult result = (GraphQueryResult) informationProduct.getResult(parameterValues);
      Repository resultRepository = Rdf4jUtils.asRepository(QueryResults.asModel(result));
      Set<Resource> subjects = getSubjects(resultRepository);

      if (subjects.isEmpty() && is404ResponseDefined()) {
        throw new NotFoundException();
      }

      GraphEntity entity =
          GraphEntity.newGraphEntity(response, resultRepository, subjects, openApi, requestContext);

      return responseOk(entity);
    }

    LOG.error("Result type {} not supported for information product {}",
        informationProduct.getResultType(), informationProduct.getIdentifier());

    return Response.serverError().build();
  }

  private Set<Resource> getSubjects(Repository repository) {
    Operation operation = apiOperation.getOperation();
    Map<String, Object> extensions = operation.getExtensions() != null
        ? operation.getExtensions() : ImmutableMap.of();
    String subjectQuery = (String) extensions.get(OpenApiSpecificationExtensions.SUBJECT_QUERY);

    if (subjectQuery == null) {
      throw new ConfigurationException(String.format(
          "Vendor extension '%s' is required for information products with graph result type",
          OpenApiSpecificationExtensions.SUBJECT_QUERY));
    }

    try {
      return Rdf4jUtils.evaluateSingleBindingSelectQuery(repository, subjectQuery);
    } catch (QueryEvaluationException qee) {
      throw new RequestHandlerRuntimeException(qee);
    }
  }

  private boolean is404ResponseDefined() {
    String statusCode = String.valueOf(Response.Status.NOT_FOUND.getStatusCode());

    return apiOperation.getOperation().getResponses().containsKey(statusCode);
  }

  private Response responseOk(Entity entity) {
    if (entity != null) {
      return Response.ok(entity).build();
    }

    return null;
  }

}
