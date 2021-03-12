package org.dotwebstack.framework.backend.json.config;

import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.ObjectTypeDefinition;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;

class JsonTypeConfigurationTest {

  @Test
  void init_throwsException_forAggregateOf() {
    JsonTypeConfiguration typeConfiguration = new JsonTypeConfiguration();

    JsonFieldConfiguration fieldConfiguration = new JsonFieldConfiguration();
    fieldConfiguration.setAggregationOf("nestedObjectList");

    typeConfiguration.setFields(Map.of("nestedObjectListAgg", fieldConfiguration));

    ObjectTypeDefinition objectTypeDefinition = newObjectTypeDefinition().name("MyObject")
        .build();

    assertThrows(InvalidConfigurationException.class, () -> typeConfiguration.init(Map.of(), objectTypeDefinition));
  }
}
