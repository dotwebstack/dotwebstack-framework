package org.dotwebstack.framework.param;

import java.util.Collection;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class ParameterUtils {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private ParameterUtils() {}

  /**
   * @throws ConfigurationException If no Parameter can be found within the supplied Parameters
   */
  public static Parameter getParameter(@NonNull Collection<Parameter> parameters,
      @NonNull String parameterId) {
    IRI iri = VALUE_FACTORY.createIRI(parameterId);

    for (Parameter<?> p : parameters) {
      if (p.getIdentifier().equals(iri)) {
        return p;
      }
    }

    throw new ConfigurationException(
        String.format("No parameter found for vendor extension value: '%s'", parameterId));
  }

}
