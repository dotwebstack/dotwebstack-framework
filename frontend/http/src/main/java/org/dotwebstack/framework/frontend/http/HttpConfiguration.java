package org.dotwebstack.framework.frontend.http;

import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.error.WebApplicationExceptionMapper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.stereotype.Service;

@Service
public class HttpConfiguration extends ResourceConfig {

  public HttpConfiguration(@NonNull List<HttpModule> httpModules) {
    register(HostPreMatchingRequestFilter.class);
    register(WebApplicationExceptionMapper.class);
    property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/(robots.txt|(assets|webjars)/.*)");
    property(ServerProperties.WADL_FEATURE_DISABLE, true);
    httpModules.forEach(module -> module.initialize(this));
  }

  public boolean resourceAlreadyRegistered(@NonNull String absolutePath, @NonNull String method) {
    return super.getResources().stream().anyMatch(
        (Resource resource) -> resource.getAllMethods().stream().map(
            ResourceMethod::getHttpMethod).anyMatch(method::equals)
            && resource.getPath().equals(absolutePath));
  }

}
