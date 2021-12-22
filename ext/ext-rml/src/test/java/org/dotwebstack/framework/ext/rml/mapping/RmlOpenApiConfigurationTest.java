package org.dotwebstack.framework.ext.rml.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.taxonic.carml.model.TriplesMap;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;

class RmlOpenApiConfigurationTest {

  @Test
  void mappingsPerOperation_isCorrectlyConstructed_forConfig() {
    var openApi = TestResources.openApi("config/openapi.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();

    Map<Operation, Set<TriplesMap>> mappingsPerOperation = rmlOpenApiConfiguration.mappingsPerOperation(openApi);

    assertThat(mappingsPerOperation.size(), is(4));
    assertThat(getTriplesMapsByPathName("/path1", openApi, mappingsPerOperation).size(), is(1));
    assertThat(getTriplesMapsByPathName("/path2", openApi, mappingsPerOperation).size(), is(2));
    assertThat(getTriplesMapsByPathName("/path3", openApi, mappingsPerOperation).size(), is(0));
    assertThat(getTriplesMapsByPathName("/path4", openApi, mappingsPerOperation).size(), is(1));
  }

  private Set<TriplesMap> getTriplesMapsByPathName(String pathName, OpenAPI openApi,
      Map<Operation, Set<TriplesMap>> mappingsPerOperation) {
    var operation = openApi.getPaths()
        .get(pathName)
        .getGet();

    return mappingsPerOperation.get(operation);
  }

  @Test
  void mappingsPerOperation_throwsException_GivenNonListMappings() {
    var openApi = TestResources.openApi("config/openapi-exception.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();

    InvalidConfigurationException invalidConfigurationException =
        assertThrows(InvalidConfigurationException.class, () -> rmlOpenApiConfiguration.mappingsPerOperation(openApi));

    assertThat(invalidConfigurationException.getMessage(),
        is("x-dws-rml-mapping on /path6 is not a list of RML mapping paths"));
  }

  @Test
  void mappingsPerOperation_throwsException_GivenNonExistentMapping() {
    var openApi = TestResources.openApi("config/openapi-exception-not-found.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();

    InvalidConfigurationException invalidConfigurationException =
        assertThrows(InvalidConfigurationException.class, () -> rmlOpenApiConfiguration.mappingsPerOperation(openApi));

    assertThat(invalidConfigurationException.getMessage(), startsWith("Could not resolve mapping file"));
  }

  @Test
  void mappingsPerOperation_throwsException_GivenMappingFileWithUnsupportedExtension() {
    var openApi = TestResources.openApi("config/openapi-exception-mapping-file-extension.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();

    InvalidConfigurationException invalidConfigurationException =
        assertThrows(InvalidConfigurationException.class, () -> rmlOpenApiConfiguration.mappingsPerOperation(openApi));

    assertThat(invalidConfigurationException.getMessage(), startsWith(
        "Could not determine rdf format for mapping filename: test3.rml.unknown. Supported file extensions are:"));
  }
}
