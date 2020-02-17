package org.dotwebstack.framework.service.openapi.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("rawtypes")
public class TypeConverterRouter {

  private List<TypeConverter> typeConverters = new ArrayList<>();

  public TypeConverterRouter(List<TypeConverter> typeConverters) {
    this.typeConverters = typeConverters;
  }

  @SuppressWarnings("unchecked")
  public Object convert(@NonNull Object source, @NonNull Map<String, Object> parameters) {
    Optional<TypeConverter> converter = typeConverters.stream()
        .filter(typeConverter -> typeConverter.supports(source))
        .findFirst();

    if (converter.isPresent()) {
      return converter.get()
          .convert(source, parameters);
    }

    return source;
  }

}
