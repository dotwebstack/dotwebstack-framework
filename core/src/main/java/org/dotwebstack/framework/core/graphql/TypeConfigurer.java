package org.dotwebstack.framework.core.graphql;

import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.graphql.scalars.Scalars;
import org.springframework.stereotype.Component;

@Component
public class TypeConfigurer implements GraphqlConfigurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(new ScalarTypeDefinition(Scalars.DATE.getName()));
    registry.add(new ScalarTypeDefinition(Scalars.DATETIME.getName()));
    registry.add(new ScalarTypeDefinition(Scalars.IRI.getName()));
  }

  @Override
  public void configureRuntimeWiring(@NonNull Builder builder) {
    builder.scalar(Scalars.DATE);
    builder.scalar(Scalars.DATETIME);
    builder.scalar(Scalars.IRI);
  }

}
