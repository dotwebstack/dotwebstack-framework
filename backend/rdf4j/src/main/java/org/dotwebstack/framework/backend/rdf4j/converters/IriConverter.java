package org.dotwebstack.framework.backend.rdf4j.converters;

import javax.annotation.Nonnull;
import lombok.NonNull;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

@Component
public class IriConverter implements CoreConverter<Value, IRI> {

  private final SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Override
  public boolean supportsValue(@NonNull Value value) {
    return IRI.class.isAssignableFrom(value.getClass());
  }

  @Override
  public boolean supportsType(@Nonnull String typeAsString) {
    return IRI.class.getSimpleName()
        .equals(typeAsString);
  }

  @Override
  public IRI convertFromValue(@NonNull Value iri) {
    return (IRI) iri;
  }

  @Override
  public Value convertToValue(@NonNull Object type) {
    return valueFactory.createIRI(String.valueOf(type));
  }
}
