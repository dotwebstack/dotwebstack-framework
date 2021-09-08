package org.dotwebstack.framework.ext.rml.mapping;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

class TestResources {

  static OpenAPI openApi(String path) {
    return new OpenAPIV3Parser().readContents(readString(path))
        .getOpenAPI();
  }

  private static String readString(String path) {
    try {
      return FileUtils.readFileToString(new File(RmlOpenApiConfigurationTest.class.getClassLoader()
          .getResource(path)
          .getFile()), "UTF-8");
    } catch (IOException e) {
      throw new IllegalArgumentException("Resource " + path + "not found.");
    }
  }
}
