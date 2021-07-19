package org.dotwebstack.framework.core.condition;

import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfigurationReader;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class GraphQlNativeEnabled implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String configFilename = context.getEnvironment()
        .getProperty("dotwebstack.config");
    if (configFilename == null) {
      configFilename = "dotwebstack.yaml";
    }
    DotWebStackConfiguration config = readConfig(configFilename);

    // if no proxy is configured, use the local/native graphql service
    return config.getSettings() == null || config.getSettings()
        .getGraphql() == null || config.getSettings()
            .getGraphql()
            .getProxy() == null;
  }

  protected DotWebStackConfiguration readConfig(String configFilename) {
    return new DotWebStackConfigurationReader().read(configFilename);
  }
}
