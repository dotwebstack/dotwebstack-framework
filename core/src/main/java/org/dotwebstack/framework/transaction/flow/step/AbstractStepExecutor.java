package org.dotwebstack.framework.transaction.flow.step;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.param.BindableParameter;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.Value;

public abstract class AbstractStepExecutor<T> implements StepExecutor {

  protected T step;

  public AbstractStepExecutor(@NonNull T step) {
    this.step = step;
  }

  protected Map<String, Value> bindParameters(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues) {
    Map<String, Value> bindings = new HashMap<>();
    for (Parameter<?> parameter : parameters) {
      String name = parameter.getName();
      Object value = parameter.handle(parameterValues);

      if (value != null && parameter instanceof BindableParameter) {
        bindings.put(name, ((BindableParameter) parameter).getValue(value));
      }
    }

    return bindings;
  }

}
