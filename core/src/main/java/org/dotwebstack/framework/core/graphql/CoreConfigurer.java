package org.dotwebstack.framework.core.graphql;

import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.graphql.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
public class CoreConfigurer implements GraphqlConfigurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(new ScalarTypeDefinition(CoreScalars.DATE.getName()));
    registry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder.scalar(CoreScalars.DATE);
    builder.scalar(CoreScalars.DATETIME);
  }

}
