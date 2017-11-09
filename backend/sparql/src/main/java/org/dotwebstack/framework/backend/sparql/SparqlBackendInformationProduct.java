package org.dotwebstack.framework.backend.sparql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;

public class SparqlBackendInformationProduct extends AbstractInformationProduct {

  private final SparqlBackend backend;

  private final String query;

  private final QueryEvaluator queryEvaluator;

  public SparqlBackendInformationProduct(Builder builder) {
    super(builder.identifier, builder.label, builder.resultType, builder.requiredParameters,
        builder.optionalParameters, builder.templateProcessor);
    this.backend = builder.backend;
    this.query = builder.query;
    this.queryEvaluator = builder.queryEvaluator;
  }

  public String getQuery() {
    return query;
  }

  @Override
  protected Object getInnerResult(Map<String, Object> parameterValues) {
    Map<String, Object> templateParameters = new HashMap<>();

    for (Parameter<?> parameter : getParameters()) {
      templateParameters.put(parameter.getName(), parameter.handle(parameterValues));
    }

    String modifiedQuery = templateProcessor.processString(query, templateParameters);

    return queryEvaluator.evaluate(backend.getConnection(), modifiedQuery);
  }

  public static class Builder {

    private final IRI identifier;

    private final SparqlBackend backend;

    private final String query;

    private final ResultType resultType;

    private final QueryEvaluator queryEvaluator;

    private final TemplateProcessor templateProcessor;

    private final Collection<Parameter<?>> requiredParameters;

    private final Collection<Parameter<?>> optionalParameters;

    private String label;

    public Builder(@NonNull IRI identifier, @NonNull SparqlBackend backend, @NonNull String query,
        @NonNull ResultType resultType, @NonNull QueryEvaluator queryEvaluator,
        @NonNull TemplateProcessor templateProcessor,
        @NonNull Collection<Parameter<?>> requiredParameters,
        @NonNull Collection<Parameter<?>> optionalParameters) {
      this.identifier = identifier;
      this.backend = backend;
      this.query = query;
      this.resultType = resultType;
      this.queryEvaluator = queryEvaluator;
      this.templateProcessor = templateProcessor;
      this.requiredParameters = requiredParameters;
      this.optionalParameters = optionalParameters;
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
