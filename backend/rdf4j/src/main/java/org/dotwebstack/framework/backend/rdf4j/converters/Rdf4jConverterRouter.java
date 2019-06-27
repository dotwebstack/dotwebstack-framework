package org.dotwebstack.framework.backend.rdf4j.converters;

import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.dotwebstack.framework.core.converters.CoreConverterRouter;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Component;

@Component
public class Rdf4jConverterRouter implements CoreConverterRouter {

  private final List<CoreConverter<Value, ?>> converters;

  public Rdf4jConverterRouter(List<CoreConverter<Value, ?>> converters) {
    this.converters = converters;
  }

  @Override
  public Object convert(Object object) {
    Optional<CoreConverter<Value, ?>> compatibleConverter = converters.stream()
        .filter(converter -> converter.supports((Value) object))
        .findFirst();

    return compatibleConverter.isPresent() ? compatibleConverter.get()
        .convert((Value) object) : DefaultConverter.convert((Value) object);
  }

}
