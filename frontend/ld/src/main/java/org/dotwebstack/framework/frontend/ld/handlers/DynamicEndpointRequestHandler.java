package org.dotwebstack.framework.frontend.ld.handlers;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpoint;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;

public class DynamicEndpointRequestHandler extends RequestHandler<DynamicEndpoint> {

  public DynamicEndpointRequestHandler(DynamicEndpoint endpoint,
      EndpointRequestParameterMapper endpointRequestParameterMapper,
      RepresentationResourceProvider representationResourceProvider) {
    super(endpoint, endpointRequestParameterMapper, representationResourceProvider);
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    Map<String, String> parameterValues = new HashMap<>();
    containerRequestContext.getUriInfo().getPathParameters().forEach(
        (key, value) -> parameterValues.put(key, value.get(0)));

    final String request = containerRequestContext.getRequest().getMethod();
    if (request.equals(HttpMethod.GET)) {
      parameterValues.putAll((endpoint.getParameterMapper().map(containerRequestContext)));
      String subjectParameter = parameterValues.get("subject");
      if (subjectParameter != null) {
        for (Representation resp : representationResourceProvider.getAll().values()) {
          String appliesTo = getUrl(resp, parameterValues);
          if (appliesTo.equals(subjectParameter)) {
            return applyRepresentation(resp, containerRequestContext, parameterValues);
          }
        }
      }
    }

    throw new ConfigurationException(String.format("Result type %s not supported for endpoint %s",
        request, endpoint.getIdentifier()));
  }

}
