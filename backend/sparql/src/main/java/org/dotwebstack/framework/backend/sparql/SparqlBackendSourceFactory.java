package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.backend.BackendSourceFactory;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendSourceFactory implements BackendSourceFactory {

  private QueryEvaluator queryEvaluator;

  @Autowired
  public SparqlBackendSourceFactory(QueryEvaluator queryEvaluator) {
    this.queryEvaluator = Objects.requireNonNull(queryEvaluator);
  }

  public BackendSource create(Backend backend, Model statements) {
    String query = Models.objectString(statements.filter(null, ELMO.QUERY, null)).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for backend source <%s>.", ELMO.QUERY,
                backend.getIdentifier())));

    return new SparqlBackendSource.Builder((SparqlBackend) backend, query, queryEvaluator).build();
  }

}
