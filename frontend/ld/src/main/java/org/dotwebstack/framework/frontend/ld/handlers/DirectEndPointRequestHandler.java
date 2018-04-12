package org.dotwebstack.framework.frontend.ld.handlers;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpoint;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectEndpointRequestHandler extends RequestHandler<DirectEndpoint> {

  private static final Logger LOG = LoggerFactory.getLogger(DirectEndpointRequestHandler.class);

  public DirectEndpointRequestHandler(DirectEndpoint endpoint,
      EndpointRequestParameterMapper endpointRequestParameterMapper,
      RepresentationResourceProvider representationResourceProvider) {
    super(endpoint, endpointRequestParameterMapper, representationResourceProvider);
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();

    Map<String, String> parameterValues = new HashMap<>();
    containerRequestContext.getUriInfo().getPathParameters().forEach(
        (key, value) -> parameterValues.put(key, value.get(0)));

    final String request = containerRequestContext.getRequest().getMethod();
    final Representation representation;
    switch (request) {
      case HttpMethod.GET:
        LOG.debug("Handling GET request for path {}", path);
        representation = endpoint.getGetRepresentation();
        break;
      case HttpMethod.POST:
        LOG.debug("Handling POST request for path {}", path);
        representation = endpoint.getPostRepresentation();
        break;
      default:
        throw new ConfigurationException(String.format(
            "Result type %s not supported for endpoint %s", request, endpoint.getIdentifier()));
    }
    return applyRepresentation(representation, containerRequestContext, parameterValues);
  }

}
