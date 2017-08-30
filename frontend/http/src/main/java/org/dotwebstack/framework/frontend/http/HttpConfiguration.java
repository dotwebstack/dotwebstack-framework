package org.dotwebstack.framework.frontend.http;

import java.util.List;
import javax.annotation.PostConstruct;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.stereotype.Service;

@Service
public class HttpConfiguration extends ResourceConfig {

  List<HttpExtension> httpExtensions;

  public HttpConfiguration(List<HttpExtension> httpExtensions) {
    super();
    this.httpExtensions = httpExtensions;
    packages("org.dotwebstack.framework.frontend.http");
    property(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/(robots.txt|assets/.*)");
    property(ServerProperties.WADL_FEATURE_DISABLE, true);
  }

  @PostConstruct
  public void initialize() {
    httpExtensions.forEach(extension -> extension.initialize(this));
  }

}
