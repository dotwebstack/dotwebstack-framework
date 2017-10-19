package org.dotwebstack.framework.backend.sparql;

import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filters.Filter;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.eclipse.rdf4j.model.IRI;

public class SparqlBackendInformationProduct extends AbstractInformationProduct {

  private SparqlBackend backend;

  private String query;

  private QueryEvaluator queryEvaluator;

  public SparqlBackendInformationProduct(Builder builder) {
    super(builder.identifier, builder.label, builder.resultType, builder.filter);
    this.backend = builder.backend;
    this.query = builder.query;
    this.queryEvaluator = builder.queryEvaluator;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public Object getResult(String value) {
    String modifiedQuery = getFilter().filter(value, this.query);
    return queryEvaluator.evaluate(backend.getConnection(), modifiedQuery);
  }

  public static class Builder {

    private IRI identifier;

    private String label;

    private SparqlBackend backend;

    private String query;

    private ResultType resultType;

    private QueryEvaluator queryEvaluator;

    private Filter filter;

    public Builder(@NonNull IRI identifier, @NonNull SparqlBackend backend, @NonNull String query,
        @NonNull ResultType resultType, @NonNull QueryEvaluator queryEvaluator, Filter filter) {
      this.identifier = identifier;
      this.backend = backend;
      this.query = query;
      this.resultType = resultType;
      this.queryEvaluator = queryEvaluator;
      this.filter = filter;
    }

    public Builder label(String label) {
      this.label = label;
      return this;
    }

    public SparqlBackendInformationProduct build() {
      return new SparqlBackendInformationProduct(this);
    }

  }

}
