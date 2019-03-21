package org.dotwebstack.framework.core.graphql.scalars;

import graphql.schema.Coercing;
import java.time.LocalDate;

class DateCoercing implements Coercing<LocalDate, LocalDate> {

  @Override
  public LocalDate serialize(Object o) {
    return null;
  }

  @Override
  public LocalDate parseValue(Object o) {
    return null;
  }

  @Override
  public LocalDate parseLiteral(Object o) {
    return null;
  }

}
