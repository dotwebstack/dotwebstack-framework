package org.dotwebstack.framework.core.converters;

import javax.annotation.Nonnull;
import lombok.NonNull;

public interface CoreConverter<V, T> {

  boolean supportsValue(@NonNull V value);

  boolean supportsType(@Nonnull String typeAsString);

  T convertFromValue(@NonNull V value);

  V convertToValue(@NonNull Object value);

}
