package org.dotwebstack.framework.backend.rdf4j;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class IriConverter implements Converter<String, IRI> {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @Override
  public IRI convert(String value) {
    return VF.createIRI(value);
  }

}
