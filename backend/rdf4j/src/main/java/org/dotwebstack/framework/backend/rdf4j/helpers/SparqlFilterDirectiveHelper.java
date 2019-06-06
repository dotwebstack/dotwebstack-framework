package org.dotwebstack.framework.backend.rdf4j.helpers;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;

public class SparqlFilterHelper {

  public static final String DEFAULT_OPERATOR = "=";

  static Type<?> getBaseType(Type<?> type) {
    if (type instanceof ListType) {
      return getBaseType((Type) type.getChildren()
          .get(0));
    }
    if (type instanceof NonNullType) {
      return getBaseType(((NonNullType) type).getType());
    }
    return type;
  }
}
