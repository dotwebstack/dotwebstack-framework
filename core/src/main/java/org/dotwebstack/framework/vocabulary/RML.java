package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class RML {

  private static final String NAMESPACE_BASE = "http://semweb.mmlab.be/ns/rml";

  private static final String NAMESPACE = NAMESPACE_BASE + "#";

  public static final IRI TRIPLES_MAP;

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    TRIPLES_MAP = valueFactory.createIRI(RML.NAMESPACE, "TriplesMap");
  }

  private RML() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", RML.class));
  }

}
