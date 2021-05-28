package org.dotwebstack.framework.core;

import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
public class CoreConfigurer implements GraphqlConfigurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(new ScalarTypeDefinition(CoreScalars.OBJECT.getName()));
    registry.add(new ScalarTypeDefinition(CoreScalars.DATE.getName()));
    registry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder.scalar(CoreScalars.OBJECT)
        .scalar(CoreScalars.DATE)
        .scalar(CoreScalars.DATETIME);
  }
}
