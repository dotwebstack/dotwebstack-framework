package org.dotwebstack.framework.extractor;


import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.exceptions.CoreException;

public class RequestExtractor implements Extractor {

  @Override
  public String extract(ContainerRequestContext containerRequestContext, String parameterName) {

    UriInfo uriInfo = containerRequestContext.getUriInfo();

    Map<String, Object> parameters = new HashMap<>();

    parameters.putAll(uriInfo.getPathParameters());
    parameters.putAll(uriInfo.getQueryParameters());
    parameters.putAll(containerRequestContext.getHeaders());

    Object parameterValue = parameters.get(parameterName);
    if (parameterValue != null) {
      return parameters.get(parameterName).toString();
    }
    throw new CoreException("No parameters found in request needed for filter.");
  }
}
