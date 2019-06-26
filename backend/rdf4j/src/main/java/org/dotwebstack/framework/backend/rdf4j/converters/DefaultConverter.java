package org.dotwebstack.framework.backend.rdf4j.converters;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

public class DefaultConverter {

  public static String convert(@NonNull Value value) {
    return value.stringValue();
  }
}
