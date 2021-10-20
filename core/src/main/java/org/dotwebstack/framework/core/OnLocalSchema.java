package org.dotwebstack.framework.core;

import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnLocalSchema implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    var resourceName = context.getEnvironment()
        .getProperty("dotwebstack.config", "dotwebstack.yaml");

    return ResourceLoaderUtils.getResource(resourceName)
        .isPresent();
  }
}
