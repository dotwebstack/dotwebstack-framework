package org.dotwebstack.framework.backend.rdf4j.config;

import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.ObjectTypeDefinition;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class Rdf4jTypeConfigurationTest {

  @Mock
  private DotWebStackConfiguration dotWebStackConfigurationMock;

  @Test
  void init_throwsException_forAggregateOf() {
    Rdf4jTypeConfiguration typeConfiguration = new Rdf4jTypeConfiguration();

    Rdf4jFieldConfiguration fieldConfiguration = new Rdf4jFieldConfiguration();
    fieldConfiguration.setAggregationOf("nestedObjectList");

    typeConfiguration.setFields(Map.of("nestedObjectListAgg", fieldConfiguration));

    ObjectTypeDefinition objectTypeDefinition = newObjectTypeDefinition().name("MyObject")
        .build();

    assertThrows(InvalidConfigurationException.class,
        () -> typeConfiguration.init(dotWebStackConfigurationMock, objectTypeDefinition));
  }
}
