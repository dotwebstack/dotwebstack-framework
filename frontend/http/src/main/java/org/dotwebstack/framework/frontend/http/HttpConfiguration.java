package org.dotwebstack.framework.frontend.http;

import java.util.List;
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
  public HttpConfiguration(List<HttpExtension> httpExtensions) {
    super();
    register(ObjectMapperProvider.class);
    packages("org.dotwebstack.framework.frontend.http");
    property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/(robots.txt|(assets|webjars)/.*)");
    property(ServerProperties.WADL_FEATURE_DISABLE, true);
    httpExtensions.forEach(extension -> extension.initialize(this));
  }

  public boolean resourceAlreadyRegistered(String absolutePath) {
    return super.getResources().stream()
        .map(Resource::getPath)
        .anyMatch(absolutePath::equals);
  }

}
