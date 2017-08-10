package org.dotwebstack.framework.config;

import java.io.IOException;
import org.eclipse.rdf4j.repository.sail.SailRepository;

public interface ConfigurationBackend {

  void initialize() throws IOException;

  SailRepository getRepository();

}
