package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class ELMO {

  public static final String NAMESPACE = "http://bp4mc2.org/elmo/def#";

  public static final IRI INFORMATION_PRODUCT;

  static {
    ValueFactory factory = SimpleValueFactory.getInstance();
    INFORMATION_PRODUCT = factory.createIRI(ELMO.NAMESPACE, "InformationProduct");
  }

  private ELMO() {
    throw new UnsupportedOperationException(
        String.format("%s is not meant to be instantiated.", ELMO.class.getName()));
  }

}
