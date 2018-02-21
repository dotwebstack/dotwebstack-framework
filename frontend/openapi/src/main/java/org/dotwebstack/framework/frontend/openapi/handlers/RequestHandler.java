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
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.Entity;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(RequestHandler.class);

  private final ApiOperation apiOperation;

  private final InformationProduct informationProduct;

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

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }

  public Map<MediaType, Property> getSchemaMap() {
    return schemaMap;
  }

  @Override
  public Response apply(@NonNull ContainerRequestContext context) {
    String path = context.getUriInfo().getPath();

    LOG.debug("Handling {} request for path {}", context.getMethod(), path);

    RequestParameters requestParameters =
        apiRequestValidator.validate(apiOperation, swagger, context);

    Map<String, String> parameterValues = requestParameterMapper.map(apiOperation.getOperation(),
        informationProduct, requestParameters);

    Response responseOk = null;
    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {

      TupleQueryResult result = (TupleQueryResult) informationProduct.getResult(parameterValues);
      TupleEntity entity =
          TupleEntity.builder().withQueryResult(result).withSchemaMap(schemaMap).build();

      responseOk = responseOk(entity);
    }
    if (ResultType.GRAPH.equals(informationProduct.getResultType())) {
      GraphQueryResult result = (GraphQueryResult) informationProduct.getResult(parameterValues);
      GraphEntity entity = GraphEntity.newGraphEntity(schemaMap, result, swagger, parameterValues,
          informationProduct);

      String query = getResultFoundQuery();

      if (query != null) {
        evaluateResultFoundQuery(entity.getModel(), query);
      }

      responseOk = responseOk(entity);
    }
    if (responseOk != null) {
      return responseOk;
    } else {
      LOG.error("Result type {} not supported for information product {}",
          informationProduct.getResultType(), informationProduct.getIdentifier());
    }
    return Response.serverError().build();
  }

  private String getResultFoundQuery() {
    String query = (String) apiOperation.getOperation().getVendorExtensions().get(
        OpenApiSpecificationExtensions.SUBJECT_QUERY);
    String statusCode = String.valueOf(Status.NOT_FOUND.getStatusCode());

    io.swagger.models.Response response404 =
        apiOperation.getOperation().getResponses().get(statusCode);

    if (query != null && response404 == null) {
      throw new ConfigurationException(
          String.format("Vendor extension '%s' has been defined, while %s response is missing",
              OpenApiSpecificationExtensions.SUBJECT_QUERY, statusCode));
    }
    if (query == null && response404 != null) {
      throw new ConfigurationException(
          String.format("Vendor extension '%s' is missing, while %s response has been defined",
              OpenApiSpecificationExtensions.SUBJECT_QUERY, statusCode));
    }

    return query;
  }

  private static void evaluateResultFoundQuery(Model model, String queryString) {
    LOG.debug("Evaluating query on model: '{}'", queryString);

    Repository repository = createRepository();

    try (RepositoryConnection connection = repository.getConnection()) {
      connection.add(model);

      BooleanQuery askQuery = connection.prepareBooleanQuery(queryString);

      if (!askQuery.evaluate()) {
        LOG.debug("Query evaluated to false. Throwing NotFoundException...");

        throw new NotFoundException();
      }
    } finally {
      repository.shutDown();
    }
  }

  private Response responseOk(Entity entity) {
    if (entity != null) {
      return Response.ok(entity).build();
    }
    return null;
  }

  private static Repository createRepository() {
    Repository repository = new SailRepository(new MemoryStore());

    repository.initialize();

    return repository;
  }

}

