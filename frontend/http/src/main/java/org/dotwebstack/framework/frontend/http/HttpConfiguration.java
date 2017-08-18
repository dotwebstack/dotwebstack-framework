package org.dotwebstack.framework.frontend.http;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.springframework.stereotype.Service;

@Service
public class HttpConfiguration extends ResourceConfig {

  // Method to allow mocking of registerResources()
  public void registerResource(Resource resource) {
    registerResources(resource);
  }

}
