package org.dotwebstack.framework.core.scalars;

import java.time.LocalDate;
import java.util.function.Supplier;

public class DateSupplier implements Supplier<LocalDate> {

  private final boolean isNow;

  private final LocalDate date;

  public DateSupplier(boolean isNow, LocalDate date) {
    this.isNow = isNow;
    this.date = date;
  }

  public DateSupplier(boolean isNow) {
    this.isNow = isNow;
    this.date = null;
  }

  @Override
  public LocalDate get() {
    if (isNow) {
      return LocalDate.now();
    }
    return date;
  }
}
