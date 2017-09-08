package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.backend.QueryType;

public class SparqlBackendSource implements BackendSource {

  private SparqlBackend backend;

  private String query;

  private QueryType queryType;

  private QueryEvaluator queryEvaluator;

  public SparqlBackendSource(Builder builder) {
    this.backend = builder.backend;
    this.query = builder.query;
    this.queryType = builder.queryType;
    this.queryEvaluator = builder.queryEvaluator;
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
    return queryEvaluator.evaluate(backend.getConnection(), query);
  }

  public QueryType getQueryType() {
    return queryType;
  }

  public static class Builder {

    private SparqlBackend backend;

    private String query;

    private QueryType queryType;

    private QueryEvaluator queryEvaluator;

    public Builder(SparqlBackend backend, String query, QueryType queryType,
        QueryEvaluator queryEvaluator) {
      this.backend = Objects.requireNonNull(backend);
      this.query = Objects.requireNonNull(query);
      this.queryType = Objects.requireNonNull(queryType);
      this.queryEvaluator = Objects.requireNonNull(queryEvaluator);
    }

    public SparqlBackendSource build() {
      return new SparqlBackendSource(this);
    }

  }

}
