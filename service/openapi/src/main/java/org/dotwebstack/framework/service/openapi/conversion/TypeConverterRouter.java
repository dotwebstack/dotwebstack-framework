package org.dotwebstack.framework.service.openapi.conversion;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("rawtypes")
public class TypeConverterRouter {

  private List<TypeConverter> typeConverters;

  public TypeConverterRouter(List<TypeConverter> typeConverters) {
    this.typeConverters = typeConverters;
  }

  @SuppressWarnings("unchecked")
  public Object convert(Object source, Map<String, Object> parameters) {
    Optional<TypeConverter> converter = typeConverters.stream()
        .filter(typeConverter -> typeConverter.supports(source))
        .findFirst();

    if (converter.isPresent()) {
      return converter.get()
          .convert(source, parameters);
    }
    throw illegalArgumentException("Could not find a converter for {}", source);
  }

}
