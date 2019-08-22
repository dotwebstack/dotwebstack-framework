package org.dotwebstack.framework.service.openapi.conversion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TypeConverterRouter {

  private List<TypeConverter<Object, Object>> typeConverters;

  public TypeConverterRouter(List<TypeConverter<Object, Object>> typeConverters) {
    this.typeConverters = typeConverters;
  }

  public Object convert(Object source, Map<String, Object> context) {
    Optional<TypeConverter<Object, Object>> converter = typeConverters.stream()
        .filter(typeConverter -> typeConverter.supports(source))
        .findFirst();

    if (converter.isPresent()) {
      return converter.get()
          .convert(source, context);
    }
    return source;
  }

}
