package org.dotwebstack.framework.frontend.ld.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.endpoint.AbstractEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPoint;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriTemplate;

public class EndPointRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(EndPointRequestHandler.class);

  private final AbstractEndPoint endpoint;

  private final EndPointRequestParameterMapper endPointRequestParameterMapper;

  private final RepresentationResourceProvider representationResourceProvider;

  public EndPointRequestHandler(@NonNull AbstractEndPoint endpoint,
      @NonNull EndPointRequestParameterMapper endPointRequestParameterMapper,
      @NonNull RepresentationResourceProvider representationResourceProvider) {
    this.endpoint = endpoint;
    this.endPointRequestParameterMapper = endPointRequestParameterMapper;
    this.representationResourceProvider = representationResourceProvider;
  }

  public EndPointRequestParameterMapper getEndPointRequestParameterMapper() {
    return endPointRequestParameterMapper;
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();

    Map<String, String> parameterValues = new HashMap<>();
    containerRequestContext.getUriInfo().getPathParameters().forEach(
        (key, value) -> parameterValues.put(key, value.get(0)));

    final String request = containerRequestContext.getRequest().getMethod();
    if (endpoint instanceof DirectEndPoint) {
      final Representation representation;
      if (request.equals(HttpMethod.GET)) {
        LOG.debug("Handling GET request for path {}", path);
        representation = ((DirectEndPoint) endpoint).getGetRepresentation();
      } else if (request.equals(HttpMethod.POST)) {
        LOG.debug("Handling POST request for path {}", path);
        representation = ((DirectEndPoint) endpoint).getPostRepresentation();
      } else {
        throw new ConfigurationException(String.format(
            "Result type %s not supported for endpoint %s", request, endpoint.getIdentifier()));
      }
      return applyRepresentation(representation, containerRequestContext, parameterValues);
    } else if (endpoint instanceof DynamicEndPoint) {
      if (request.equals(HttpMethod.GET)) {
        parameterValues.putAll(
            ((DynamicEndPoint) endpoint).getParameterMapper().map(containerRequestContext));
        Optional<String> subjectParameter = Optional.ofNullable(parameterValues.get("subject"));
        if (subjectParameter.isPresent()) {
          for (Representation resp : representationResourceProvider.getAll().values()) {
            String appliesTo = getUrl(resp, parameterValues);
            if (appliesTo.equals(subjectParameter.get())) {
              return applyRepresentation(resp, containerRequestContext, parameterValues);
            }
          }
        }
      }
    } else {
      throw new ConfigurationException(
          String.format("Unsupported endpoint typ {%s} for endpoint {%s}", endpoint.getClass(),
              endpoint.getIdentifier()));
    }
    throw new ConfigurationException(String.format("Result type %s not supported for endpoint %s",
        request, endpoint.getIdentifier()));
  }

  private String getUrl(Representation representation, Map<String, String> parameterValues) {
    String url = "";
    for (String appliesTo : representation.getAppliesTo()) {
      UriTemplate template = new UriTemplate(appliesTo);
      url = template.expand(parameterValues).toString();
    }
    return url;
  }

  private Response applyRepresentation(@NonNull Representation representation,
      @NonNull ContainerRequestContext containerRequestContext, @NonNull Map parameterValues) {

    InformationProduct informationProduct = representation.getInformationProduct();

    endPointRequestParameterMapper.map(informationProduct, containerRequestContext).forEach(
        (key, value) -> parameterValues.put(key, value));
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
