package org.dotwebstack.framework.core;

import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.scalars.Scalars;
import org.springframework.stereotype.Component;

@Component
public class CoreConfigurer implements Configurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(new ScalarTypeDefinition(Scalars.DATE.getName()));
    registry.add(new ScalarTypeDefinition(Scalars.DATETIME.getName()));
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder.scalar(Scalars.DATE);
    builder.scalar(Scalars.DATETIME);
  }

}
