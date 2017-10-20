package org.dotwebstack.framework.backend.sparql;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.eclipse.rdf4j.model.IRI;

public class SparqlBackendInformationProduct extends AbstractInformationProduct {

  private final SparqlBackend backend;

  private final String query;

  private final QueryEvaluator queryEvaluator;

  public SparqlBackendInformationProduct(Builder builder) {
    super(builder.identifier, builder.label, builder.resultType, builder.filters);
    this.backend = builder.backend;
    this.query = builder.query;
    this.queryEvaluator = builder.queryEvaluator;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public Object getResult(Map<String, String> values) {
    String modifiedQuery = query;

    for (Filter filter : filters) {
      String value = values.get(filter.getName());

      if (value == null) {
        // TODO What exception to throw here?
        throw new RuntimeException(String.format(
            "No value found for filter '%s'. Available values: %s", filter.getName(), values));
      }

      modifiedQuery = filter.filter(value, modifiedQuery);
    }

    return queryEvaluator.evaluate(backend.getConnection(), modifiedQuery);
  }

  public static class Builder {

    private final IRI identifier;

    private final SparqlBackend backend;

    private final String query;

    private final ResultType resultType;

    private final QueryEvaluator queryEvaluator;

    private final Collection<Filter> filters;

    private String label;

    public Builder(@NonNull IRI identifier, @NonNull SparqlBackend backend, @NonNull String query,
        @NonNull ResultType resultType, @NonNull QueryEvaluator queryEvaluator,
        @NonNull Collection<Filter> filters) {
      this.identifier = identifier;
      this.backend = backend;
      this.query = query;
      this.resultType = resultType;
      this.queryEvaluator = queryEvaluator;
      this.filters = filters;
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
