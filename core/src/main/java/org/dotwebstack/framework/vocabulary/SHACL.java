package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class SHACL {

  public static final IRI DATATYPE;

  public static final IRI DEFAULT_VALUE;

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    DATATYPE = valueFactory.createIRI("http://www.w3.org/ns/shacl#", "datatype");
    DEFAULT_VALUE = valueFactory.createIRI("http://www.w3.org/ns/shacl#", "defaultValue");
  }

  private SHACL() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", SHACL.class));
  }

}
