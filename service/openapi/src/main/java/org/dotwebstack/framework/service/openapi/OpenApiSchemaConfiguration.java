package org.dotwebstack.framework.service.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.util.ResolverFully;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.dotwebstack.framework.core.ResourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiSchemaConfiguration {

  private static final String SPEC_FILENAME = "openapi.yaml";

  @Bean
  public InputStream openApiStream() throws FileNotFoundException {
    URI location = ResourceProperties.getFileConfigPath()
        .resolve(SPEC_FILENAME);
    var path = Paths.get(location);
    if (Files.exists(path)) {
      return new FileInputStream(path.toFile());
    }
    return getClass().getResourceAsStream(ResourceProperties.getResourcePath()
        .resolve(SPEC_FILENAME)
        .getPath());
  }

  @Bean
  public OpenAPI openApi() {
    var openApi = new OpenAPIV3Parser().read(ResourceProperties.getResourcePath()
        .resolve(SPEC_FILENAME)
        .getPath());

    new ResolverFully().resolveFully(openApi);

    return openApi;
  }
}
