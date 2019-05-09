package org.dotwebstack.framework.core.directives;

import lombok.NonNull;
import org.dotwebstack.framework.core.DotWebStackRuntimeException;

public class DirectiveValidationException extends DotWebStackRuntimeException {

  static final long serialVersionUID = -916368070377395351L;

  public DirectiveValidationException(@NonNull String message, Object... arguments) {
    super(message, arguments);
  }

}
