package org.dotwebstack.framework.core.converters;

public interface CoreConverterRouter {

  Object convertFromValue(Object object);

  Object convertToValue(Object value, String typeAsString);

}
