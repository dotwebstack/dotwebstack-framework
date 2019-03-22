package org.dotwebstack.framework.core;

import graphql.schema.idl.RuntimeWiring;

public interface Configurer {

  default void configureRuntimeWiring(RuntimeWiring.Builder builder) {

  }

}
