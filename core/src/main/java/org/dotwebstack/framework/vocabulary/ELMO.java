package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class ELMO {

  public static final String NAMESPACE = "http://bp4mc2.org/elmo/def#";

  public static final IRI PRODUCT;

  static {
    ValueFactory factory = SimpleValueFactory.getInstance();
    PRODUCT = factory.createIRI(ELMO.NAMESPACE, "Product");
  }

  private ELMO() {
    throw new UnsupportedOperationException("Vocabulary classes are not meant to be instantiated.");
  }

}
