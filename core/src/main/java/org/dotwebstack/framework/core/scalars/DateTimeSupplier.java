package org.dotwebstack.framework.core.scalars;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

public class DateTimeSupplier implements Supplier<ZonedDateTime> {

  private final boolean isNow;

  private final ZonedDateTime dateTime;

  public DateTimeSupplier(boolean isNow, ZonedDateTime dateTime) {
    this.isNow = isNow;
    this.dateTime = dateTime;
  }

  public DateTimeSupplier(boolean isNow) {
    this.isNow = isNow;
    this.dateTime = null;
  }

  @Override
  public ZonedDateTime get() {
    if (isNow) {
      return ZonedDateTime.now();
    }
    return dateTime;
  }
}
