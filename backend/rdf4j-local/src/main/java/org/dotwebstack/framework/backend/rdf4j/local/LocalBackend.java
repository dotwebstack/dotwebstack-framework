package org.dotwebstack.framework.backend.rdf4j.local;

import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.eclipse.rdf4j.repository.sail.SailRepository;

@RequiredArgsConstructor
public class LocalBackend implements Rdf4jBackend<SailRepository> {

  public static final String LOCAL_BACKEND_NAME = "local";

  private final SailRepository repository;

  @Override
  public SailRepository getRepository() {
    return repository;
  }

}

