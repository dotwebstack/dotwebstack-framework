package org.dotwebstack.framework.test;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public final class DBEERPEDIA {

  public static final String NAMESPACE = "http://dbeerpedia.org#";

  public static final String OBJECT_NAMESPACE = "http://dbeerpedia.org/id/";

  public static final IRI BACKEND;

  public static final Literal BACKEND_LABEL;

  public static final Literal ENDPOINT;

  public static final IRI BREWERIES;

  public static final IRI BROUWTOREN;


  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    BACKEND = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Backend");
    BACKEND_LABEL = valueFactory.createLiteral("Beer breweries in The Netherlands");
    ENDPOINT = valueFactory.createLiteral("http://localhost:8080/sparql", XMLSchema.ANYURI);
    BREWERIES = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Breweries");
    BROUWTOREN = valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE,
        "brewery/900e5c1c-d292-48c8-b9bd-1baf02ee2d2c");
  }

  private DBEERPEDIA() {
    throw new UnsupportedOperationException(
        String.format("%s is not meant to be instantiated.", DBEERPEDIA.class));
  }

}
