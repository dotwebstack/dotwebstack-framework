package org.dotwebstack.framework.core.condition;

import static graphql.Assert.assertNotNull;
import static graphql.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ConfigurationCondition;

@ExtendWith(MockitoExtension.class)
public class GraphQlNativeDisabledTest {

  @Spy
  private GraphQlNativeDisabled condition;

  @Test
  void initConditionObject() {

    var condition1 = new GraphQlNativeDisabled();
    assertNotNull(condition1);
    var configPhase = condition1.getConfigurationPhase();
    assertTrue(configPhase.equals(ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION));

    var condition2 = new GraphQlNativeDisabled(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN);
    assertNotNull(condition2);
    var configPhase2 = condition2.getConfigurationPhase();
    assertTrue(configPhase2.equals(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN));

  }
}
