package org.dotwebstack.framework.backend.rdf4j.converters;

import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.dotwebstack.framework.core.converters.CoreConverterRouter;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Component;

@Component
public class Rdf4jConverterRouter implements CoreConverterRouter {

  private final List<CoreConverter<?>> converters;

  public Rdf4jConverterRouter(List<CoreConverter<?>> converters) {
    this.converters = converters;
  }

  @Override
  public Object convert(Object object) {
    Optional<CoreConverter<?>> compatibleConverter = converters.stream()
        .filter(converter -> converter.supports(object))
        .findFirst();

    return compatibleConverter.isPresent() ? compatibleConverter.get()
        .convert(object) : DefaultConverter.convert((Value) object);
  }

}
