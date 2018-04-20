package org.dotwebstack.framework.frontend.openapi.handlers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.SwaggerUtils;
import org.glassfish.jersey.process.Inflector;

public class OpenApiSpecHandler implements Inflector<ContainerRequestContext, String> {

  private String openApiSpec;

  public OpenApiSpecHandler(@NonNull String yaml) throws IOException {
    YAMLMapper mapper = new YAMLMapper();
    InputStream input = new ByteArrayInputStream(yaml.getBytes("UTF-8"));
    ObjectNode specNode = SwaggerUtils.removeVendorExtensions(input, mapper);
    openApiSpec = mapper.writer().writeValueAsString(specNode);
  }

  @Override
  public String apply(ContainerRequestContext data) {
    return openApiSpec;
  }

}
