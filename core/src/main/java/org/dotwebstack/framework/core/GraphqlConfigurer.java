package org.dotwebstack.framework.core;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;

public interface GraphqlConfigurer {

  default void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    // Default empty method for simplicity and backwards compatibility
  }

  default void configureRuntimeWiring(@NonNull RuntimeWiring.Builder builder) {
    // Default empty method for simplicity and backwards compatibility
  }

}
