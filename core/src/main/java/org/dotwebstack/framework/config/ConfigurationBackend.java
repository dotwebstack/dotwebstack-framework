package org.dotwebstack.framework.config;

import org.eclipse.rdf4j.repository.sail.SailRepository;

public interface ConfigurationBackend {

  SailRepository getRepository();

}
