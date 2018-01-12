package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class Shacl {

  public static final IRI DATATYPE;

  static {
    DATATYPE =
        SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/shacl#", "datatype");
  }
}
