package org.dotwebstack.framework.core.config.validators;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.GraphQlSettingsConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(GraphQlNativeEnabled.class)
@Component
public class SettingsValidator implements DotWebStackConfigurationValidator {

  @Override
  public void validate(DotWebStackConfiguration dotWebStackConfiguration) {
    if (dotWebStackConfiguration.getSettings() != null && isProxy(dotWebStackConfiguration.getSettings()
        .getGraphql())) {
      if (!dotWebStackConfiguration.getQueries()
          .isEmpty()) {
        throw new InvalidConfigurationException("Queries should not be configured when using a graphql proxy");
      } else if (!dotWebStackConfiguration.getSubscriptions()
          .isEmpty()) {
        throw new InvalidConfigurationException("Subscriptions should not be configured when using a graphql proxy");
      } else if (!dotWebStackConfiguration.getObjectTypes()
          .isEmpty()) {
        throw new InvalidConfigurationException("Object types should not be configured when using a graphql proxy");
      }
    }
  }

  private boolean isProxy(GraphQlSettingsConfiguration graphql) {
    return graphql != null && graphql.getProxy() != null;
  }
}
