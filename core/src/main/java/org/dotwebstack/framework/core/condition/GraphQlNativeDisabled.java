package org.dotwebstack.framework.core.condition;

import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.context.annotation.Conditional;

@SuppressWarnings("unused")
public class GraphQlNativeDisabled extends NoneNestedConditions {

  public GraphQlNativeDisabled() {
    super(ConfigurationPhase.PARSE_CONFIGURATION);
  }

  public GraphQlNativeDisabled(ConfigurationPhase configurationPhase) {
    super(configurationPhase);
  }

  @Conditional(GraphQlNativeEnabled.class)
  static class OnGraphQlNativeEnabled {

  }
}
