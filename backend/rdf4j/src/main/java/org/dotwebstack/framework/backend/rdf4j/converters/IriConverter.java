package org.dotwebstack.framework.backend.rdf4j.converters;

import lombok.NonNull;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

@Component
public class IriConverter implements CoreConverter<IRI> {

  @Override
  public boolean supports(@NonNull Object value) {
    return IRI.class.isAssignableFrom(value.getClass());
  }

  @Override
  public IRI convert(@NonNull Object iri) {
    return (IRI) iri;
  }
}
