package org.dotwebstack.framework.core;

import graphql.schema.idl.RuntimeWiring.Builder;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.SourceDirectiveWiring;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CoreConfigurer implements Configurer {

  private final SourceDirectiveWiring sourceDirectiveWiring;

  @Override
  public void configureRuntimeWiring(Builder builder) {
    builder.directive(CoreDirectives.SOURCE_NAME, sourceDirectiveWiring);
    builder.scalar(CoreScalars.DATE);
    builder.scalar(CoreScalars.DATETIME);
  }

}
