package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.AbstractBackend;
import org.eclipse.rdf4j.model.IRI;

class SparqlBackend extends AbstractBackend {

  private String endpoint;

  public SparqlBackend(IRI identifier, String endpoint) {
    super(identifier);
    this.endpoint = Objects.requireNonNull(endpoint);
  }

  public String getEndpoint() {
    return endpoint;
  }

}
