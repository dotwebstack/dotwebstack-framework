package org.dotwebstack.framework.service.openapi.param;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ParamHandlerRouter {
  private List<ParamHandler> customHandlers;

  private ParamHandler defaultHandler;

  public ParamHandlerRouter(List<ParamHandler> customHandlers) {
    this.customHandlers = customHandlers;
    this.defaultHandler = new DefaultParamHandler();
  }

  public ParamHandler getParamHandler(@NonNull Parameter parameter) {
    return this.customHandlers.stream()
        .filter(handler -> handler.supports(parameter))
        .findFirst()
        .orElse(this.defaultHandler);
  }
}
