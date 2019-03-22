package org.dotwebstack.framework.core;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;

public interface Configurer {

  default void configureTypeDefinitionRegistry(TypeDefinitionRegistry registry) {
    // Default empty method for simplicity and backwards compatibility
  }

  default void configureRuntimeWiring(RuntimeWiring.Builder builder) {
    // Default empty method for simplicity and backwards compatibility
  }

}
