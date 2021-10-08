package org.dotwebstack.framework.core.config.validators;

import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.model.GraphQlSettings;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.stereotype.Component;

@Component
public class SettingsValidator implements SchemaValidator {

  @Override
  public void validate(Schema schema) {
    if (schema.getSettings() != null && isProxy(schema.getSettings()
        .getGraphql())) {
      if (isPresent(schema.getQueries())) {
        throw new InvalidConfigurationException("Queries should not be configured when using a graphql proxy");
      } else if (isPresent(schema.getSubscriptions())) {
        throw new InvalidConfigurationException("Subscriptions should not be configured when using a graphql proxy");
      } else if (isPresent(schema.getObjectTypes())) {
        throw new InvalidConfigurationException("Object types should not be configured when using a graphql proxy");
      }
    }
  }

  private boolean isPresent(Map<?, ?> map) {
    return map != null && !map.isEmpty();
  }

  private boolean isProxy(GraphQlSettings graphql) {
    return graphql != null && graphql.getProxy() != null;
  }
}
