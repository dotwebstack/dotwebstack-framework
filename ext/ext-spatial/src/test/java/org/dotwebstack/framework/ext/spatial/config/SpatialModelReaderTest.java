package org.dotwebstack.framework.ext.spatial.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.ext.spatial.model.Schema;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.dotwebstack.framework.ext.spatial.testhelper.TestSpatialReferenceSystemDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpatialModelReaderTest {

  @Test
  void read_returnsSchema_forConfigFile() {
    String configFile = "dotwebstack/dotwebstack-spatial.yaml";

    var deserializerModule =
        new SimpleModule().addDeserializer(SpatialReferenceSystem.class, new TestSpatialReferenceSystemDeserializer());
    var objectMapper = new ObjectMapper(new YAMLFactory()).registerModule(deserializerModule);
    SpatialModelReader spatialModelReader = new SpatialModelReader(objectMapper);

    var result = spatialModelReader.read(configFile);

    assertThat(result, is(notNullValue()));
    assertThat(result.getSpatial(), is(notNullValue()));
    assertThat(result.getSpatial()
        .getReferenceSystems(), is(notNullValue()));
    assertThat(result.getSpatial()
        .getReferenceSystems()
        .size(), is(4));
    assertThat(result.getSpatial()
        .isUseWorkaroundForIntersects(), is(true));
  }

  @Test
  void read_throwsException_forNonExistingConfiguration() {
    var objectMapper = new ObjectMapper();
    var spatialModelReader = new SpatialModelReader(objectMapper);

    var exception = assertThrows(InvalidConfigurationException.class, () -> spatialModelReader.read("a/b.yaml"));

    assertThat(exception.getMessage(), is("Config file not found on location: a/b.yaml"));
  }

  @Test
  void read_throwsException_forReadValueFault() throws IOException {
    String configFile = "dotwebstack/dotwebstack-spatial.yaml";

    var objectMapper = mock(ObjectMapper.class);
    when(objectMapper.readValue(any(InputStream.class), eq(Schema.class))).thenThrow(IOException.class);
    var spatialModelReader = new SpatialModelReader(objectMapper);

    var exception = assertThrows(InvalidConfigurationException.class, () -> spatialModelReader.read(configFile));

    assertThat(exception.getMessage(), is("Error while reading config file."));
  }
}
