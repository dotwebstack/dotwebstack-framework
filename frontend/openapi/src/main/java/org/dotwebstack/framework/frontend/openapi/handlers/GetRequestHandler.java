package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
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

  private final RequestParameterMapper requestParameterMapper;

  GetRequestHandler(@NonNull Operation operation, @NonNull InformationProduct informationProduct,
      @NonNull RequestParameterMapper requestParameterMapper) {
    this.operation = operation;
    this.informationProduct = informationProduct;
    this.requestParameterMapper = requestParameterMapper;
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }


  @Override
  public Response apply(@NonNull ContainerRequestContext context) {
    String path = context.getUriInfo().getPath();
    LOG.debug("Handling GET request for path {}", path);

    Map<String, Object> parameterValues =
        requestParameterMapper.map(operation, informationProduct, context);
    io.swagger.models.Response response = operation.getResponses().get("200");
    Property schemaProperty = response.getSchema();

    Response responseOk = null;
    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      org.eclipse.rdf4j.query.TupleQueryResult result =
          (org.eclipse.rdf4j.query.TupleQueryResult) informationProduct.getResult(parameterValues);

      TupleEntity entity = (TupleEntity) (TupleEntity.builder().withSchemaProperty(
          schemaProperty).withRequestParameters(
              RequestParameters.builder().requestStringParameters(
                  parameterValues).build()).withQueryResult(
                      QueryResult.builder().withQueryResultTriple(result).build()).withBaseUri(
                          context.getUriInfo().getBaseUri().toString()).withPath(
                              context.getUriInfo().getPath())).build();
      responseOk = responseOk(entity);
    }
    if (ResultType.GRAPH.equals(informationProduct.getResultType())) {
      org.eclipse.rdf4j.query.GraphQueryResult result =
          (org.eclipse.rdf4j.query.GraphQueryResult) informationProduct.getResult(parameterValues);
      GraphEntity entity = (GraphEntity) GraphEntity.builder().withSchemaProperty(
          schemaProperty).withRequestParameters(
              RequestParameters.builder().requestStringParameters(
                  parameterValues).build()).withQueryResult(
                      QueryResult.builder().withQueryResultGraph(result).build()).withBaseUri(
                          context.getUriInfo().getBaseUri().toString()).withPath(
                              context.getUriInfo().getPath()).build();
      responseOk = responseOk(entity);
    }
    if (responseOk != null) {
      return responseOk;
    }
    return Response.serverError().build();
  }

  private Response responseOk(Entity entity) {
    if (entity != null) {
      return Response.ok(entity).build();
    }
    return null;
  }

}

