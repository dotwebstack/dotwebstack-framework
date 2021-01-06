package org.dotwebstack.framework.backend.rdf4j;

import java.util.Map;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;

interface ConfigFactory {

  RepositoryImplConfig create(String type, Map<String, Object> args);

  void registerRepositoryType(String type, ConfigCreator creator);

  @FunctionalInterface
  interface ConfigCreator {

    RepositoryImplConfig create(Map<String, Object> args);

  }

}
