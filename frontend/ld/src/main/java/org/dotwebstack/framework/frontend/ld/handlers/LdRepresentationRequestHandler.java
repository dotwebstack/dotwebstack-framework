package org.dotwebstack.framework.frontend.ld.handlers;

import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdRepresentationRequestHandler
    implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(LdRepresentationRequestHandler.class);

  private Representation representation;

  private EndPointRequestParameterMapper ldRepresentationRequestParameterMapper;

  public LdRepresentationRequestHandler(@NonNull Representation representation,
      @NonNull EndPointRequestParameterMapper ldRepresentationRequestParameterMapper) {
    this.representation = representation;
    this.ldRepresentationRequestParameterMapper = ldRepresentationRequestParameterMapper;
  }

  public EndPointRequestParameterMapper getLdRepresentationRequestParameterMapper() {
    return ldRepresentationRequestParameterMapper;
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();

    InformationProduct informationProduct = representation.getInformationProduct();

    Map<String, String> parameterValues =
        ldRepresentationRequestParameterMapper.map(informationProduct, containerRequestContext);

    representation.getParameterMappers().forEach(
        parameterMapper -> parameterValues.putAll(parameterMapper.map(containerRequestContext)));

    Object result = informationProduct.getResult(parameterValues);

    if (ResultType.GRAPH.equals(informationProduct.getResultType())) {
      return Response.ok(new GraphEntity((GraphQueryResult) result, representation)).build();
    }
    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      return Response.ok(new TupleEntity((TupleQueryResult) result, representation)).build();
    }

    throw new ConfigurationException(
        String.format("Result type %s not supported for information product %s",
            informationProduct.getResultType(), informationProduct.getIdentifier()));
  }

}
