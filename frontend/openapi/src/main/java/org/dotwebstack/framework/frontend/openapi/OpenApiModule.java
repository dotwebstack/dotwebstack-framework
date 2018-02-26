package org.dotwebstack.framework.frontend.openapi;

import java.io.IOException;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.dotwebstack.framework.frontend.openapi.cors.CorsResponseFilter;
import org.dotwebstack.framework.frontend.openapi.entity.EntityWriterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class OpenApiModule implements HttpModule {

  private OpenApiRequestMapper requestMapper;

  @Autowired
  public OpenApiModule(@NonNull OpenApiRequestMapper requestMapper) {
    this.requestMapper = requestMapper;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    httpConfiguration.register(EntityWriterInterceptor.class);
    httpConfiguration.register(CorsResponseFilter.class);

    try {
      requestMapper.map(httpConfiguration);
    } catch (IOException exp) {
      throw new ConfigurationException("Failed loading OpenAPI definitions.", exp);
    }
  }

}
