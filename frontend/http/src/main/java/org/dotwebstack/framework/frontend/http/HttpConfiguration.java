package org.dotwebstack.framework.frontend.http;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.springframework.stereotype.Service;

@Service
public class HttpConfiguration extends ResourceConfig {

  public boolean resourceAlreadyRegistered(String absolutePath) {
    return super.getResources().stream()
        .map(Resource::getPath)
        .anyMatch(absolutePath::equals);
  }

}
