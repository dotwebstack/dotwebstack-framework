package org.dotwebstack.framework.transaction.flow;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.param.Parameter;

public interface FlowExecutor {

  void execute(@NonNull Collection<Parameter> parameters,
      @NonNull Map<String, String> parameterValues);

}
