package org.dotwebstack.framework.core.directives;

import lombok.NonNull;
import org.dotwebstack.framework.core.DotWebStackRuntimeException;

class DirectiveValidationException extends DotWebStackRuntimeException {

  static final long serialVersionUID = -916368070377395351L;

  DirectiveValidationException(@NonNull String message, Object... arguments) {
    super(message, arguments);
  }

}
