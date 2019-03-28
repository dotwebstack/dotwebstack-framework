package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import graphql.schema.idl.SchemaDirectiveWiring;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.core.Backend;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

@RequiredArgsConstructor
abstract class AbstractDirectiveWiring implements SchemaDirectiveWiring {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final BackendRegistry backendRegistry;

  Rdf4jBackend getBackend(String backendName) {
    Backend backend = backendRegistry.get(backendName);

    if (!(backend instanceof Rdf4jBackend)) {
      throw new InvalidConfigurationException(
          String.format("Backend '%s' not found or is not an RDF4J backend.", backendName));
    }

    return (Rdf4jBackend) backend;
  }

}
