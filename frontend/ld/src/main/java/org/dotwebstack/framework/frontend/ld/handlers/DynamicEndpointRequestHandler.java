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
import org.springframework.web.util.UriTemplate;

public class DynamicEndpointRequestHandler extends RequestHandler<DynamicEndpoint> {

  public DynamicEndpointRequestHandler(DynamicEndpoint endpoint,
      EndpointRequestParameterMapper endpointRequestParameterMapper,
      RepresentationResourceProvider representationResourceProvider) {
    super(endpoint, endpointRequestParameterMapper, representationResourceProvider);
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    Map<String, String> parameterValues = new HashMap<>();
    if (containerRequestContext.getUriInfo().getQueryParameters() != null) {
      containerRequestContext.getUriInfo().getQueryParameters().forEach(
          (key, value) -> value.forEach(val -> parameterValues.put(key, val)));
    }
    containerRequestContext.getUriInfo().getPathParameters().forEach(
        (key, value) -> value.forEach(val -> parameterValues.put(key, val)));

    final String request = containerRequestContext.getRequest().getMethod();
    if (request.equals(HttpMethod.GET)) {
      if (endpoint.getParameterMapper() != null) {
        parameterValues.putAll((endpoint.getParameterMapper().map(containerRequestContext)));
      }
      String subjectParameter = parameterValues.get("subject");
      if (endpoint.getStage() == null) {
        throw new ConfigurationException(
            String.format("Endpoint {%s} has no stage", endpoint.getIdentifier()));
      }
      if (subjectParameter != null) {
        for (Representation resp : representationResourceProvider.getAll().values()) {
          if (endpoint.getParameterMapper() != null) {
            String appliesTo = getUrl(resp, parameterValues, containerRequestContext);
            if (appliesTo.equals(subjectParameter)
                && endpoint.getStage().getIdentifier().equals(resp.getStage().getIdentifier())) {
              return applyRepresentation(resp, containerRequestContext, parameterValues);
            }
          } else {
            for (String appliesTo : resp.getAppliesTo()) {
              UriTemplate template = new UriTemplate(appliesTo);
              if (template.matches(subjectParameter)
                  && endpoint.getStage().getIdentifier().equals(resp.getStage().getIdentifier())) {
                return applyRepresentation(resp, containerRequestContext, parameterValues);
              }
            }
          }
        }
        throw new ConfigurationException(
            String.format("Found no representation for {%s} subject {%s}", endpoint.getIdentifier(),
                subjectParameter));
      }
    }
    throw new ConfigurationException(String.format("Result type %s not supported for endpoint %s",
        request, endpoint.getIdentifier()));
  }

}
