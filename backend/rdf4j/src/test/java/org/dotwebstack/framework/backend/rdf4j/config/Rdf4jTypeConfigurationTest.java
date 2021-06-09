package org.dotwebstack.framework.backend.rdf4j.config;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Rdf4jTypeConfigurationTest {

  @Mock
  private DotWebStackConfiguration dotWebStackConfigurationMock;

  @Test
  void init_throwsException_forAggregateOf() {
    Rdf4jTypeConfiguration typeConfiguration = new Rdf4jTypeConfiguration();

    Rdf4jFieldConfiguration fieldConfiguration = new Rdf4jFieldConfiguration();
    fieldConfiguration.setAggregationOf("nestedObjectList");

    typeConfiguration.setFields(Map.of("nestedObjectListAgg", fieldConfiguration));

    assertThrows(InvalidConfigurationException.class, () -> typeConfiguration.init(dotWebStackConfigurationMock));
  }
}
