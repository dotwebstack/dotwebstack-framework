package org.dotwebstack.framework.frontend.http;

import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.jackson.ObjectMapperProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HttpConfiguration extends ResourceConfig {

  @Autowired
  public HttpConfiguration(List<HttpModule> httpModules,
      SupportedMediaTypesScanner supportedMediaTypesScanner) {
    super();
    supportedMediaTypesScanner.getGraphQueryWriters().forEach(this::register);
    supportedMediaTypesScanner.getTupleQueryWriters().forEach(this::register);

    register(ObjectMapperProvider.class);
    register(HostPreMatchingRequestFilter.class);
    property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/(robots.txt|(assets|webjars)/.*)");
    property(ServerProperties.WADL_FEATURE_DISABLE, true);
    httpModules.forEach(module -> module.initialize(this));
  }

  public boolean resourceAlreadyRegistered(@NonNull String absolutePath) {
    return super.getResources().stream().map(Resource::getPath).anyMatch(absolutePath::equals);
  }

}
