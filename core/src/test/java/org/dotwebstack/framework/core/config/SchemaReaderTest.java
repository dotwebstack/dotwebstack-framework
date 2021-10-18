package org.dotwebstack.framework.core.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.TestHelper;
import org.dotwebstack.framework.core.model.Schema;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SchemaReaderTest {

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
  void read_returnsSchema_forConfigFile() throws IOException {
    var result = schemaReader.read("dotwebstack/dotwebstack-queries.yaml");

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Schema);
  }
}
