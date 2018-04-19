package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class R2RML {

  private static final String NAMESPACE_BASE = "http://www.w3.org/ns/r2rml";

  private static final String NAMESPACE = NAMESPACE_BASE + "#";

  public static final IRI TRIPLES_MAP;

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    TRIPLES_MAP = valueFactory.createIRI(R2RML.NAMESPACE, "TriplesMap");
  }

  private R2RML() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", R2RML.class));
  }

}
