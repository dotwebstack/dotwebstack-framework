package org.dotwebstack.framework.backend.sparql.informationproduct;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.BindableParameter;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlBackendInformationProduct extends AbstractInformationProduct {

  private static final Logger LOG = LoggerFactory.getLogger(SparqlBackendInformationProduct.class);

  private final SparqlBackend backend;

  private final String query;

  private final QueryEvaluator queryEvaluator;

  public SparqlBackendInformationProduct(Builder builder) {
    super(builder.identifier, builder.label, builder.resultType, builder.parameters,
        builder.templateProcessor);
    this.backend = builder.backend;
    this.query = builder.query;
    this.queryEvaluator = builder.queryEvaluator;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public Object getResult(@NonNull Map<String, String> parameterValues) {
    Map<String, Object> templateParameters = new HashMap<>();
    Map<String, Value> bindings = new HashMap<>();

    for (Parameter<?> parameter : getParameters()) {
      String name = parameter.getName();
      Object value = parameter.handle(parameterValues);

      if (value != null) {
        templateParameters.put(name, value);

        if (parameter instanceof BindableParameter) {
          bindings.put(name, ((BindableParameter) parameter).getValue(value));
        }
      }
    }

    String modifiedQuery = templateProcessor.processString(query, templateParameters);

    LOG.debug("Query after templating: '{}'", modifiedQuery);

    return queryEvaluator.evaluate(backend.getConnection(), modifiedQuery, bindings);
  }

  public static class Builder {

    private final Resource identifier;

    private final SparqlBackend backend;

    private final String query;

    private final ResultType resultType;

    private final QueryEvaluator queryEvaluator;

    private final TemplateProcessor templateProcessor;

    private final Collection<Parameter> parameters;

    private String label;

    public Builder(@NonNull Resource identifier, @NonNull SparqlBackend backend,
        @NonNull String query, @NonNull ResultType resultType,
        @NonNull QueryEvaluator queryEvaluator, @NonNull TemplateProcessor templateProcessor,
        @NonNull Collection<Parameter> parameters) {
      this.identifier = identifier;
      this.backend = backend;
      this.query = query;
      this.resultType = resultType;
      this.queryEvaluator = queryEvaluator;
      this.templateProcessor = templateProcessor;
      this.parameters = parameters;
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
