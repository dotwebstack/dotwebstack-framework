package org.dotwebstack.framework.frontend.openapi;

import java.io.IOException;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenApiModule implements HttpModule {

  private OpenApiRequestMapper requestMapper;

  @Autowired
  public OpenApiModule(OpenApiRequestMapper requestMapper) {
    this.requestMapper = requestMapper;
  }

  @Override
  public void initialize(HttpConfiguration httpConfiguration) {
    try {
      requestMapper.map(httpConfiguration);
    } catch (IOException e) {
      throw new ConfigurationException("Failed loading OpenAPI definitions.", e);
    }
  }

}
