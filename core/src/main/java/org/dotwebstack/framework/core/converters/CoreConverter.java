package org.dotwebstack.framework.core.converters;

import lombok.NonNull;

public interface CoreConverter<T> {

  boolean supports(@NonNull Object object);

  T convert(@NonNull Object value);

}
