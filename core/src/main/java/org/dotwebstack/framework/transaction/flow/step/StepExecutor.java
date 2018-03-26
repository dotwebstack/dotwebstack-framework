package org.dotwebstack.framework.transaction.flow.step;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.param.Parameter;

public interface StepExecutor {

  void execute(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues);

}
