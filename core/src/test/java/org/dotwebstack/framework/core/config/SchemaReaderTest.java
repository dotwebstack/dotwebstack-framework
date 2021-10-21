package org.dotwebstack.framework.core.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.TestHelper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaReaderTest {

  @Mock
  private ObjectMapper objectMapper;

  private SchemaReader schemaReader;

  @BeforeEach
  void doBefore() {
    objectMapper = TestHelper.createObjectMapper();
    schemaReader = new SchemaReader(objectMapper);
  }

  @Test
  void read_throwsException_ifNotConfigFile() {
    var exception = assertThrows(InvalidConfigurationException.class, () -> schemaReader.read("a/b.yaml"));

    assertThat(exception.getMessage(), CoreMatchers.is("Config file not found on location: a/b.yaml"));
  }

  @Test
  void read_returnsSchema_forConfigFile() {
    String configFile = "dotwebstack/dotwebstack-objecttypes.yaml";

    var result = schemaReader.read(configFile);
    assertThat(result.getObjectTypes()
        .get("Brewery")
        .getFields()
        .get("identifier")
        .getType(), CoreMatchers.is("ID"));
  }
}
