package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class SHACL {

  public static final IRI DATATYPE;

  static {
    DATATYPE =
        SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/shacl#", "datatype");
  }

  private SHACL() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", SHACL.class));
  }

}
