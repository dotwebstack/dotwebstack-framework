package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.backend.BackendSource;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;

public class SparqlBackendSource implements BackendSource {

  private SparqlBackend backend;

  private String query;

  public SparqlBackendSource(Builder builder) {
    this.backend = builder.backend;
    this.query = builder.query;
  }

  @Override
  public Backend getBackend() {
    return backend;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public Object getResult() {
    Query preparedQuery;

    try {
      preparedQuery = backend.getConnection().prepareQuery(QueryLanguage.SPARQL, query);
    } catch (RDF4JException e) {
      throw new BackendException(String.format("Query could not be prepared: %s", query), e);
    }

    if (preparedQuery instanceof GraphQuery) {
      try {
        return ((GraphQuery) preparedQuery).evaluate();
      } catch (QueryEvaluationException e) {
        throw new BackendException(String.format("Query could not be evaluated: %s", query), e);
      }
    }

    if (preparedQuery instanceof TupleQuery) {
      try {
        return ((TupleQuery) preparedQuery).evaluate();
      } catch (QueryEvaluationException e) {
        throw new BackendException(String.format("Query could not be evaluated: %s", query), e);
      }
    }

    throw new BackendException(
        String.format("Query type '%s' not supported.", preparedQuery.getClass()));
  }

  public static class Builder {

    private SparqlBackend backend;

    private String query;

    public Builder(SparqlBackend backend, String query) {
      this.backend = Objects.requireNonNull(backend);
      this.query = Objects.requireNonNull(query);
    }

    public SparqlBackendSource build() {
      return new SparqlBackendSource(this);
    }

  }

}
