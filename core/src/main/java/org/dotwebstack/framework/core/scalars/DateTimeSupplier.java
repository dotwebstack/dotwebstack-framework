package org.dotwebstack.framework.core.scalars;

import java.time.OffsetDateTime;
import java.util.function.Supplier;

public class DateTimeSupplier implements Supplier<OffsetDateTime> {

  private final boolean isNow;

  private final OffsetDateTime dateTime;

  public DateTimeSupplier(boolean isNow, OffsetDateTime dateTime) {
    this.isNow = isNow;
    this.dateTime = dateTime;
  }

  @Override
  public OffsetDateTime get() {
    if (isNow) {
      return OffsetDateTime.now();
    }
    return dateTime;
  }
}
