package org.dotwebstack.framework.core.graphql.proxy;

import lombok.NonNull;
import org.dotwebstack.framework.core.DotWebStackRuntimeException;

public class GraphQlProxyException extends DotWebStackRuntimeException {

  private static final long serialVersionUID = 7760665084527035669L;

  public GraphQlProxyException(@NonNull String message, Object... arguments) {
    super(message, arguments);
  }

  public GraphQlProxyException(@NonNull String message, Throwable cause, Object... arguments) {
    super(message, cause, arguments);
  }
}
