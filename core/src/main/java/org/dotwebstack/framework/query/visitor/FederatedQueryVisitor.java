package org.dotwebstack.framework.query.visitor;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FederatedQueryVisitor extends AbstractQueryModelVisitor<RuntimeException> {

  private static final Logger LOG = LoggerFactory.getLogger(FederatedQueryVisitor.class);

  private BackendResourceProvider backendResourceProvider;

  private Map<Resource, Backend> replaceableBackends;

  public FederatedQueryVisitor(@NonNull BackendResourceProvider backendResourceProvider) {
    this.backendResourceProvider = backendResourceProvider;
    replaceableBackends = new HashMap<>();
  }

  public Map<Resource, Backend> getReplaceableBackends() {
    return replaceableBackends;
  }

  @Override
  public void meet(Service service) {
    final Resource backendIri = (Resource) service.getServiceRef().getValue();
    final Backend backend = backendResourceProvider.get(backendIri);
    if (backend == null) {
      throw new ConfigurationException(String.format("Backend {%s} not found.", backendIri));
    }
    replaceableBackends.put(backendIri, backend);
    LOG.debug("Add backend {} to map of replaceableBackends map", backendIri);
  }

}
