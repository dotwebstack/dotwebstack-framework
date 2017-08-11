package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class ELMO {

  public static final String NAMESPACE = "http://dotwebstack.org/def/elmo#";

  public static final IRI INFORMATION_PRODUCT;

  public static final IRI BACKEND;

  public static final IRI SPARQL_BACKEND;

  public static final IRI ENDPOINT;

  public static final IRI BACKEND_PROP;

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    // Classes
    INFORMATION_PRODUCT = valueFactory.createIRI(ELMO.NAMESPACE, "InformationProduct");
    BACKEND = valueFactory.createIRI(ELMO.NAMESPACE, "Backend");
    SPARQL_BACKEND = valueFactory.createIRI(ELMO.NAMESPACE, "SparqlBackend");
    ENDPOINT = valueFactory.createIRI(ELMO.NAMESPACE, "Endpoint");

    // Properties
    BACKEND_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "backend");
  }

  private ELMO() {
    throw new UnsupportedOperationException(
        String.format("%s is not meant to be instantiated.", ELMO.class));
  }

}
