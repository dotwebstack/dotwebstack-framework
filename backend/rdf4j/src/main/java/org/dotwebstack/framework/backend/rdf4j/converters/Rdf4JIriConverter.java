package org.dotwebstack.framework.backend.rdf4j.converters;

import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

@Component
public class Rdf4JIriConverter implements CoreConverter<IRI> {

  @Override
  public boolean supports(Object value) {
    return IRI.class.isAssignableFrom(value.getClass());
  }

  @Override
  public IRI convert(Object iri) {
    return (IRI) iri;
  }
}
