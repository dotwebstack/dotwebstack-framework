package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

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
  public Object convertFromValue(Object object) {
    Optional<CoreConverter<Value, ?>> compatibleConverter = converters.stream()
        .filter(converter -> converter.supportsValue((Value) object))
        .findFirst();

    return compatibleConverter.isPresent() ? compatibleConverter.get()
        .convertFromValue((Value) object) : DefaultConverter.convert((Value) object);
  }

  @Override
  public Value convertToValue(Object value, String typeAsString) {
    return converters.stream()
        .filter(converter -> converter.supportsType(typeAsString))
        .findFirst()
        .map(converter -> converter.convertToValue(value))
        .orElseThrow(() -> invalidConfigurationException("Unsupported argument type: {}", typeAsString));
  }

}
