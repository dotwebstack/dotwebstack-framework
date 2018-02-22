package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class SHACL {

  public static final IRI DATATYPE;

  public static final IRI DEFAULT_VALUE;

  public static final IRI NODEKIND;

  private static final String NAMESPACE_BASE = "http://www.w3.org/ns/shacl";

  private static final String NAMESPACE = NAMESPACE_BASE + "#";

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    DATATYPE = valueFactory.createIRI(SHACL.NAMESPACE, "datatype");
    DEFAULT_VALUE = valueFactory.createIRI(SHACL.NAMESPACE, "defaultValue");

    NODEKIND = valueFactory.createIRI(SHACL.NAMESPACE, "nodeKind");
  }

  private SHACL() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", SHACL.class));
  }

}
