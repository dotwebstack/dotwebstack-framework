package org.dotwebstack.framework.frontend.openapi;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public final class Rdf4jUtils {

  private Rdf4jUtils() {}

  /**
   * @return An in-memory {@code Repository} for the supplied {@code Model}.
   */
  public static Repository asRepository(@NonNull Model model) {
    Repository repository = new SailRepository(new MemoryStore());

    repository.initialize();

    try (RepositoryConnection connection = repository.getConnection()) {
      connection.add(model);
    }

    return repository;
  }

}
