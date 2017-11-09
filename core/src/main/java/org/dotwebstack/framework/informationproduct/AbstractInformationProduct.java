package org.dotwebstack.framework.informationproduct;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractInformationProduct implements InformationProduct {

  protected final IRI identifier;

  protected final String label;

  protected final ResultType resultType;

  protected final Collection<Parameter<?>> requiredParameters;

  protected final Collection<Parameter<?>> optionalParameters;

  protected final TemplateProcessor templateProcessor;

  protected AbstractInformationProduct(@NonNull IRI identifier, String label,
      @NonNull ResultType resultType, @NonNull Collection<Parameter<?>> requiredParameters,
      @NonNull Collection<Parameter<?>> optionalParameters,
      @NonNull TemplateProcessor templateProcessor) {
    this.identifier = identifier;
    this.resultType = resultType;
    this.label = label;
    this.requiredParameters = ImmutableList.copyOf(requiredParameters);
    this.optionalParameters = ImmutableList.copyOf(optionalParameters);
    this.templateProcessor = templateProcessor;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public ResultType getResultType() {
    return resultType;
  }

  @Override
  public Collection<Parameter<?>> getParameters() {
    List<Parameter<?>> result = new ArrayList<>(requiredParameters);

    result.addAll(optionalParameters);

    return result;
  }

  @Override
  public Object getResult(@NonNull Map<String, Object> parameterValues) {
    for (Parameter<?> parameter : requiredParameters) {
      if (parameterValues.get(parameter.getName()) == null) {
        throw new BackendException(String.format(
            "No value found for required parameter '%s'. Supplied parameterValues: %s",
            parameter.getName(), parameterValues));
      }
    }

    return

    getInnerResult(parameterValues);
  }

  protected abstract Object getInnerResult(@NonNull Map<String, Object> parameterValues);

}
