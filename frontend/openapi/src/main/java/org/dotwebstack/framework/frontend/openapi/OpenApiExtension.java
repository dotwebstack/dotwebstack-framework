package org.dotwebstack.framework.frontend.openapi;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenApiExtension {

  private SwaggerImporter swaggerImporter;

  @Autowired
  public OpenApiExtension(SwaggerImporter swaggerImporter) {
    this.swaggerImporter = swaggerImporter;
  }

  @PostConstruct
  public void postLoad() throws IOException {
    swaggerImporter.importDefinitions();
  }

}
