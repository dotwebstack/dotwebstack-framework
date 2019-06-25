package org.dotwebstack.framework.backend.rdf4j.converters;

import org.eclipse.rdf4j.model.Literal;

public class DefaultConverter {

  public static String convert(Object value) {
    return ((Literal) value).stringValue();
  }
}
