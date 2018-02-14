package org.dotwebstack.framework.informationproduct;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.Resource;

public abstract class AbstractInformationProduct implements InformationProduct {

  protected final Resource identifier;

  protected final String label;

  protected final ResultType resultType;

  protected final Collection<Parameter> parameters;

  protected final TemplateProcessor templateProcessor;

  protected AbstractInformationProduct(@NonNull Resource identifier, String label,
      @NonNull ResultType resultType, @NonNull Collection<Parameter> parameters,
      @NonNull TemplateProcessor templateProcessor) {
    this.identifier = identifier;
    this.resultType = resultType;
    this.label = label;
    this.parameters = ImmutableList.copyOf(parameters);
    this.templateProcessor = templateProcessor;
  }

  @Override
  public Resource getIdentifier() {
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
  public Collection<Parameter> getParameters() {
    return parameters;
  }
}
