package org.dotwebstack.framework.core;

import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(GraphQlNativeEnabled.class)
@Component
public class CoreConfigurer implements GraphqlConfigurer {

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  public CoreConfigurer(TypeDefinitionRegistry typeDefinitionRegistry) {
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    typeDefinitionRegistry.add(new ScalarTypeDefinition(CoreScalars.OBJECT.getName()));
    typeDefinitionRegistry.add(new ScalarTypeDefinition(CoreScalars.DATE.getName()));
    typeDefinitionRegistry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder.scalar(CoreScalars.OBJECT)
        .scalar(CoreScalars.DATE)
        .scalar(CoreScalars.DATETIME);
  }
}
