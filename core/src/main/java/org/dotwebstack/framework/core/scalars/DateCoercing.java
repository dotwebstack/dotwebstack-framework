package org.dotwebstack.framework.core.scalars;

import java.time.LocalDate;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
class LocalDateCoercing extends BaseCoercing<LocalDate> {

  LocalDateCoercing() {
    super(LocalDate.class);
  }

  @Override
  public LocalDate parse(@NonNull String value) {
    return LocalDate.parse(value);
  }
}
