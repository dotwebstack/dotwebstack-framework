package org.dotwebstack.framework.frontend.http;

import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.error.GenericExceptionMapper;
import org.dotwebstack.framework.frontend.http.error.WebApplicationExceptionMapper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.stereotype.Service;

@Service
public class HttpConfiguration extends ResourceConfig {

  public HttpConfiguration(@NonNull List<HttpModule> httpModules) {
    register(RequestIdFilter.class, 10);
    register(MdcRequestIdFilter.class, 11);
    register(HostPreMatchingRequestFilter.class);
    register(WebApplicationExceptionMapper.class);
    register(GenericExceptionMapper.class);

    property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/(robots.txt|(assets|webjars)/.*)");
    property(ServerProperties.WADL_FEATURE_DISABLE, true);

    httpModules.forEach(module -> module.initialize(this));
  }

  public boolean resourceAlreadyRegistered(@NonNull String absolutePath, @NonNull String method) {
    return getResources().stream().anyMatch(
        resource -> resource.getAllMethods().stream().map(ResourceMethod::getHttpMethod).anyMatch(
            method::equals) && resource.getPath().equals(absolutePath));
  }

}
