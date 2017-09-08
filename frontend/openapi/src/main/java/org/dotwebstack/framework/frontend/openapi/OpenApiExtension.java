package org.dotwebstack.framework.frontend.openapi;

import java.io.IOException;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenApiExtension implements HttpExtension {

  private SwaggerImporter swaggerImporter;

  @Autowired
  public OpenApiExtension(@NonNull SwaggerImporter swaggerImporter) {
    this.swaggerImporter = swaggerImporter;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    try {
      swaggerImporter.importDefinitions(httpConfiguration);
    } catch (IOException e) {
      throw new ConfigurationException("Failed loading OpenAPI definitions.", e);
    }
  }

}
