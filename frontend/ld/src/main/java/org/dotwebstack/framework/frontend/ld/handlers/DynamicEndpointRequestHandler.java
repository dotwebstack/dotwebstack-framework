package org.dotwebstack.framework.frontend.ld.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpoint;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.springframework.web.util.UriTemplate;

public class DynamicEndpointRequestHandler extends RequestHandler<DynamicEndpoint> {

  public DynamicEndpointRequestHandler(DynamicEndpoint endpoint,
      EndpointRequestParameterMapper endpointRequestParameterMapper,
      RepresentationResourceProvider representationResourceProvider) {
    super(endpoint, endpointRequestParameterMapper, representationResourceProvider);
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    final String request = containerRequestContext.getRequest().getMethod();
    if (request.equals(HttpMethod.GET)) {
      Map<String, String> parameterValues = new HashMap<>();
      Optional.ofNullable(containerRequestContext.getUriInfo().getQueryParameters()).ifPresent(
          parameters -> parameters.forEach(
              (key, value) -> value.forEach(val -> parameterValues.put(key, val))));

      containerRequestContext.getUriInfo().getPathParameters().forEach(
          (key, value) -> value.forEach(val -> parameterValues.put(key, val)));
      Optional.ofNullable(endpoint.getParameterMapper()).ifPresent(
          mapper -> parameterValues.putAll((mapper.map(containerRequestContext))));

      String subjectParameter = parameterValues.get("subject");
      if (endpoint.getStage() == null) {
        throw new ConfigurationException(
            String.format("Endpoint {%s} has no stage", endpoint.getIdentifier()));
      }
      for (Representation resp : representationResourceProvider.getAll().values()) {
        if (endpoint.getParameterMapper() != null) {
          String appliesTo = getUrl(resp, parameterValues, containerRequestContext);
          if (appliesTo.equals(subjectParameter)
              && endpoint.getStage().getIdentifier().equals(resp.getStage().getIdentifier())) {
            return applyRepresentation(resp, containerRequestContext, parameterValues);
          }
        } else if (match(resp, subjectParameter)) {
          return applyRepresentation(resp, containerRequestContext, parameterValues);
        }
      }
      throw new ConfigurationException(
          String.format("Found no representation for {%s} subject {%s}", endpoint.getIdentifier(),
              subjectParameter));
    }
    throw new ConfigurationException(String.format("Result type %s not supported for endpoint %s",
        request, endpoint.getIdentifier()));
  }

  private boolean match(Representation representation, String subjectParameter) {
    boolean match = false;
    if (subjectParameter != null) {
      for (String appliesTo : representation.getAppliesTo()) {
        UriTemplate template = new UriTemplate(appliesTo);
        match = (template.matches(subjectParameter) && endpoint.getStage().getIdentifier().equals(
            representation.getStage().getIdentifier()));
      }
    }
    return match;
  }

}
