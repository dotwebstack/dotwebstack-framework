package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.openapi.entity.Entity;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GetRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(GetRequestHandler.class);

  private final Operation operation;

  private final InformationProduct informationProduct;

  private final Map<MediaType, Property> schemaMap;

  private final RequestParameterMapper requestParameterMapper;

  GetRequestHandler(@NonNull Operation operation, @NonNull InformationProduct informationProduct,
      @NonNull Map<MediaType, Property> schemaMap,
      @NonNull RequestParameterMapper requestParameterMapper) {
    this.operation = operation;
    this.informationProduct = informationProduct;
    this.schemaMap = schemaMap;
    this.requestParameterMapper = requestParameterMapper;
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
    LOG.debug("Handling GET request for path {}", path);



    Map<String, String> parameterValues =
        requestParameterMapper.map(operation, informationProduct, context);
    org.eclipse.rdf4j.query.QueryResult result = (org.eclipse.rdf4j.query.QueryResult) informationProduct.getResult(parameterValues);



    io.swagger.models.Response response = operation.getResponses().get("200");
    Property schemaProperty = response.getSchema();



    RequestParameters requestParameters = new RequestParameters();

    requestParameters.putAll(context.getUriInfo().getQueryParameters());
    requestParameters.putAll(context.getUriInfo().getPathParameters());

    Entity entity = null;
    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      entity = new TupleEntity(schemaMap, schemaProperty, requestParameters,
          QueryResult.builder().withQueryResultDb(result).build(), context.getUriInfo().getBaseUri().toString(), context.getUriInfo().getPath());
    }
    if (ResultType.GRAPH.equals(informationProduct.getResultType())) {
      entity = new GraphEntity(schemaMap, schemaProperty, requestParameters,
              QueryResult.builder().withQueryResultDb(result).build(), context.getUriInfo().getBaseUri().toString(), context.getUriInfo().getPath());
    }

    if (entity != null) {
      return Response.ok(entity).build();
    }

    return Response.serverError().build();
  }

}

