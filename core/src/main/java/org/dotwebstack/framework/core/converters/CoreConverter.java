package org.dotwebstack.framework.core.converters;

import lombok.NonNull;

public interface CoreConverter<O, T> {

  boolean supports(@NonNull O object);

  T convert(@NonNull O object);

}
