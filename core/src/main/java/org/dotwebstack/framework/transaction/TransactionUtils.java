package org.dotwebstack.framework.transaction;

import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class TransactionUtils {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private TransactionUtils() {}

  /**
   * @throws ConfigurationException If no Parameter can be found within the supplied
   *         Transaction for the given ID.
   */
  public static Parameter getParameter(@NonNull Transaction transaction,
      @NonNull String parameterId) {
    IRI iri = VALUE_FACTORY.createIRI((String) parameterId);

    for (Parameter<?> p : transaction.getParameters()) {
      if (p.getIdentifier().equals(iri)) {
        return p;
      }
    }

    throw new ConfigurationException(
        String.format("No parameter found for vendor extension value: '%s'", parameterId));
  }

}
