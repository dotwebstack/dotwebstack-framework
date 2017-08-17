package org.dotwebstack.framework;

import java.io.IOException;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class TestConfigurationBackend implements ConfigurationBackend {

  private SailRepository sailRepository;

  public TestConfigurationBackend() throws IOException {
    this.initialize();
  }

  @Override
  public void initialize() throws IOException {
    MemoryStore sail = new MemoryStore();

    sailRepository = new SailRepository(sail);
    sailRepository.initialize();

    clearAllData();
  }

  private void clearAllData() {
    try(RepositoryConnection connection = sailRepository.getConnection()) {
      connection.clear();
    }
  }

  @Override
  public SailRepository getRepository() {
    return sailRepository;
  }

  public void addModel(Model model) {
    try(RepositoryConnection connection = sailRepository.getConnection()) {
      connection.add(model);
      connection.commit();
    }
  }
}
