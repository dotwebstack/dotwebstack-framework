package org.dotwebstack.framework.core.converters;

public interface CoreConverter<T> {

  boolean supports(Object object);

  T convert(Object value);

}
