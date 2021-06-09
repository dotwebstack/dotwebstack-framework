package org.dotwebstack.framework.backend.json.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.junit.jupiter.api.Test;

class JsonTypeConfigurationTest {

  @Test
  void init_throwsException_forAggregateOf() {
    JsonTypeConfiguration typeConfiguration = new JsonTypeConfiguration();

    JsonFieldConfiguration fieldConfiguration = new JsonFieldConfiguration();
    fieldConfiguration.setAggregationOf("nestedObjectList");

    typeConfiguration.setFields(Map.of("nestedObjectListAgg", fieldConfiguration));

    assertThrows(InvalidConfigurationException.class,
        () -> typeConfiguration.init(mock(DotWebStackConfiguration.class)));
  }
}
