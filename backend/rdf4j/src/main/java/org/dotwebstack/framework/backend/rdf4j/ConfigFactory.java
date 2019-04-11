package org.dotwebstack.framework.backend.rdf4j;

import java.util.Map;
import java.util.function.Function;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;

interface ConfigFactory {

  RepositoryImplConfig create(String type, Map<String, Object> args);

  void registerRepositoryType(String type,
      Function<Map<String, Object>, RepositoryImplConfig> creator);

}
