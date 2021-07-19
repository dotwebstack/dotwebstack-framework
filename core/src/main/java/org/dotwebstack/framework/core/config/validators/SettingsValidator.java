package org.dotwebstack.framework.core.config.validators;

import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.GraphQlSettingsConfiguration;
import org.springframework.stereotype.Component;

@Component
public class SettingsValidator implements DotWebStackConfigurationValidator {

  @Override
  public void validate(DotWebStackConfiguration dotWebStackConfiguration) {
    if (dotWebStackConfiguration.getSettings() != null && isProxy(dotWebStackConfiguration.getSettings()
        .getGraphql())) {
      if (isPresent(dotWebStackConfiguration.getQueries())) {
        throw new InvalidConfigurationException("Queries should not be configured when using a graphql proxy");
      } else if (isPresent(dotWebStackConfiguration.getSubscriptions())) {
        throw new InvalidConfigurationException("Subscriptions should not be configured when using a graphql proxy");
      } else if (isPresent(dotWebStackConfiguration.getObjectTypes())) {
        throw new InvalidConfigurationException("Object types should not be configured when using a graphql proxy");
      }
    }
  }

  private boolean isPresent(Map<?, ?> map) {
    return map != null && !map.isEmpty();
  }

  private boolean isProxy(GraphQlSettingsConfiguration graphql) {
    return graphql != null && graphql.getProxy() != null;
  }
}
