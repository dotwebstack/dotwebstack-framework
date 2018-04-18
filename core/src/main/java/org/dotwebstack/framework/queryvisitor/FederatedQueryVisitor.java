package org.dotwebstack.framework.queryvisitor;

import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

public class FederatedQueryVisitor extends AbstractQueryModelVisitor {

  private BackendResourceProvider backendResourceProvider;

  private Backend backend;

  public FederatedQueryVisitor(@NonNull BackendResourceProvider backendResourceProvider) {
    this.backendResourceProvider = backendResourceProvider;
  }

  public Backend getBackend() {
    return backend;
  }

  @Override
  public void meet(Service service) {
    final Resource backendIir = (Resource) service.getServiceRef().getValue();
    backend = Optional.of(backendResourceProvider.get(backendIir)).orElseThrow(
        () -> new ConfigurationException(String.format("Backend {%s} not found.", backendIir)));
  }

}
