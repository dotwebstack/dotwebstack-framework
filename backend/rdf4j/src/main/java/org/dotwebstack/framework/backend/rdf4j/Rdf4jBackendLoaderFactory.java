package org.dotwebstack.framework.backend.rdf4j;

import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.model.ObjectType;
import org.eclipse.rdf4j.repository.Repository;
import org.springframework.stereotype.Component;

@Component
public class Rdf4jBackendLoaderFactory implements BackendLoaderFactory {

  private final Repository repository;

  private final NodeShapeRegistry nodeShapeRegistry;

  public Rdf4jBackendLoaderFactory(Repository repository, NodeShapeRegistry nodeShapeRegistry) {
    this.repository = repository;
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public <T extends ObjectType<?>> BackendLoader create(T objectType) {
    var nodeShape = nodeShapeRegistry.get(objectType.getName());
    return new Rdf4jBackendLoader(repository, nodeShape);
  }
}
