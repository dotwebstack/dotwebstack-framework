package org.dotwebstack.framework.frontend.openapi.handlers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecUtils;
import org.glassfish.jersey.process.Inflector;

public class OpenApiSpecHandler implements Inflector<ContainerRequestContext, String> {

  private String openApiSpec;

  public OpenApiSpecHandler(@NonNull String yaml) throws IOException {
    YAMLMapper mapper = new YAMLMapper();
    InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
    ObjectNode specNode = OpenApiSpecUtils.removeVendorExtensions(input, mapper);
    openApiSpec = mapper.writer().writeValueAsString(specNode);
  }

  @Override
  public String apply(ContainerRequestContext data) {
    return openApiSpec;
  }

}
