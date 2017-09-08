package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.eclipse.rdf4j.model.IRI;

public class SparqlBackendInformationProduct extends AbstractInformationProduct {

  private final SparqlBackend backend;

  private final String query;

  private final QueryEvaluator queryEvaluator;

  public String getQuery() {
    return query;
  }

  @Override
  public Object getResult() {
    return queryEvaluator.evaluate(backend.getConnection(), query);
  }

  public SparqlBackendInformationProduct(Builder builder) {
    super(builder.identifier, builder.label, builder.resultType);
    this.backend = builder.backend;
    this.query = builder.query;
    this.queryEvaluator = builder.queryEvaluator;
  }

  public static class Builder {

    private IRI identifier;

    private String label;

    private SparqlBackend backend;

    private String query;

    private ResultType resultType;

    private QueryEvaluator queryEvaluator;

    public Builder(IRI identifier, SparqlBackend backend, String query, ResultType resultType,
        QueryEvaluator queryEvaluator) {
      this.identifier = Objects.requireNonNull(identifier);
      this.backend = Objects.requireNonNull(backend);
      this.query = Objects.requireNonNull(query);
      this.resultType = Objects.requireNonNull(resultType);
      this.queryEvaluator = Objects.requireNonNull(queryEvaluator);
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
