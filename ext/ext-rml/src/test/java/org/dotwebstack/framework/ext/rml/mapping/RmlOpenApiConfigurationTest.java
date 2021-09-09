package org.dotwebstack.framework.ext.rml.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.taxonic.carml.model.TriplesMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.junit.jupiter.api.Test;

class RmlOpenApiConfigurationTest {

  @Test
  void mappingsPerOperation_isCorrectlyConstructed_forConfig() {
    // Arrange
    var openApi = TestResources.openApi("config/openapi.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();

    // Act
    Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation =
        rmlOpenApiConfiguration.mappingsPerOperation(openApi);

    // Assert
    assertThat(mappingsPerOperation.size(), is(4));
    assertThat(getTriplesMapsByPathName("/path1", mappingsPerOperation).size(), is(1));
    assertThat(getTriplesMapsByPathName("/path2", mappingsPerOperation).size(), is(2));
    assertThat(getTriplesMapsByPathName("/path3", mappingsPerOperation).size(), is(0));
    assertThat(getTriplesMapsByPathName("/path4", mappingsPerOperation).size(), is(1));
  }

  private Set<TriplesMap> getTriplesMapsByPathName(String pathName,
      Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation) {
    return mappingsPerOperation.entrySet()
        .stream()
        .filter(entry -> entry.getKey()
            .getName()
            .equals(pathName))
        .map(Map.Entry::getValue)
        .flatMap(Set::stream)
        .collect(Collectors.toUnmodifiableSet());
  }

  @Test
  void mappingsPerOperation_throwsException_GivenNonListMappings() {
    // Arrange
    var openApi = TestResources.openApi("config/openapi-exception.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();

    // Act
    InvalidConfigurationException invalidConfigurationException =
        assertThrows(InvalidConfigurationException.class, () -> rmlOpenApiConfiguration.mappingsPerOperation(openApi));

    // Assert
    assertThat(invalidConfigurationException.getMessage(),
        is("x-dws-rml-mapping on /path6 is not a list of RML mapping paths"));
  }

  @Test
  void mappingsPerOperation_throwsException_GivenNonExistentMapping() {
    // Arrange
    var openApi = TestResources.openApi("config/openapi-exception-not-found.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();

    // Act
    InvalidConfigurationException invalidConfigurationException =
        assertThrows(InvalidConfigurationException.class, () -> rmlOpenApiConfiguration.mappingsPerOperation(openApi));

    // Assert
    assertThat(invalidConfigurationException.getMessage(), startsWith("Could not resolve mapping file"));
  }

  @Test
  void mappingsPerOperation_throwsException_GivenMappingFileWithUnsupportedExtension() {
    // Arrange
    var openApi = TestResources.openApi("config/openapi-exception-mapping-file-extension.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();

    // Act
    InvalidConfigurationException invalidConfigurationException =
        assertThrows(InvalidConfigurationException.class, () -> rmlOpenApiConfiguration.mappingsPerOperation(openApi));

    // Assert
    assertThat(invalidConfigurationException.getMessage(), startsWith(
        "Could not determine rdf format for mapping filename: test3.rml.unknown. Supported file extensions are:"));
  }
}
