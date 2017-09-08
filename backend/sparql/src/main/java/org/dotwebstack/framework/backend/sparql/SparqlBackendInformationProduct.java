package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.IRI;

public class SparqlBackendInformationProduct extends InformationProduct {

  private final InformationProduct informationProduct;

  private final SparqlBackend backend;

  private final String query;

  private final ResultType resultType;

  private final QueryEvaluator queryEvaluator;

  public String getQuery() {
    return query;
  }

  @Override
  public ResultType getResultType() {
    return resultType;
  }

  @Override
  public IRI getIdentifier() {
    return informationProduct.getIdentifier();
  }

  @Override
  public String getLabel() {
    return informationProduct.getLabel();
  }

  @Override
  public Object getResult() {
    return queryEvaluator.evaluate(backend.getConnection(), query);
  }

  public SparqlBackendInformationProduct(Builder builder) {
    this.informationProduct = builder.informationProduct;
    this.backend = builder.backend;
    this.query = builder.query;
    this.resultType = builder.resultType;
    this.queryEvaluator = builder.queryEvaluator;
  }

  public static class Builder {

    private InformationProduct informationProduct;

    private SparqlBackend backend;

    private String query;

    private ResultType resultType;

    private QueryEvaluator queryEvaluator;

    public Builder(InformationProduct informationProduct, SparqlBackend backend, String query,
        ResultType resultType, QueryEvaluator queryEvaluator) {
      this.informationProduct = Objects.requireNonNull(informationProduct);
      this.backend = Objects.requireNonNull(backend);
      this.query = Objects.requireNonNull(query);
      this.resultType = Objects.requireNonNull(resultType);
      this.queryEvaluator = Objects.requireNonNull(queryEvaluator);
    }

    public SparqlBackendInformationProduct build() {
      return new SparqlBackendInformationProduct(this);
    }

  }

}
