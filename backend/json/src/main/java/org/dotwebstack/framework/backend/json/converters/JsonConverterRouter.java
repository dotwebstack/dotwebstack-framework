package org.dotwebstack.framework.backend.json.converters;

import org.dotwebstack.framework.core.converters.CoreConverterRouter;
import org.springframework.stereotype.Component;

@Component
public class JsonConverterRouter implements CoreConverterRouter {
  @Override
  public Object convertFromValue(Object object) {
    throw new UnsupportedOperationException("not yet implemented.");
  }

  @Override
  public Object convertToValue(Object value, String typeAsString) {
    throw new UnsupportedOperationException("not yet implemented.");
  }
}

