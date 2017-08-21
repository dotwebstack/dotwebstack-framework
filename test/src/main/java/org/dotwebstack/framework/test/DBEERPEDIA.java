package org.dotwebstack.framework.test;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public final class DBEERPEDIA {

  public static final String NAMESPACE = "http://dbeerpedia.org#";

  public static final String OBJECT_NAMESPACE = "http://dbeerpedia.org/id/";

  public static final String URL_PATTERN = "/test/url/pattern";

  public static final IRI BACKEND;

  public static final IRI SECOND_BACKEND;

  public static final Literal ENDPOINT;

  public static final IRI BREWERIES;

  public static final Literal BREWERIES_LABEL;

  public static final Literal WINERIES_LABEL;

  public static final Literal SELECT_ALL_QUERY;

  public static final IRI BROUWTOREN;

  public static final IRI PERCENTAGES_INFORMATION_PRODUCT;

  public static final IRI ORIGIN_INFORMATION_PRODUCT;

  public static final String OPENAPI_DESCRIPTION = "DBeerpedia API";

  public static final String OPENAPI_HOST = "dbpeerpedia.org";

  @java.lang.SuppressWarnings("squid:S1075")
  public static final String OPENAPI_BASE_PATH = "/api/v1";

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    BACKEND = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Backend");
    SECOND_BACKEND = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "SecondBackend");
    ENDPOINT = valueFactory.createLiteral("http://localhost:8080/sparql", XMLSchema.ANYURI);
    BREWERIES = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Breweries");
    BREWERIES_LABEL = valueFactory.createLiteral("Beer breweries in The Netherlands");
    WINERIES_LABEL = valueFactory.createLiteral("Wineries in The Netherlands");
    SELECT_ALL_QUERY = valueFactory.createLiteral("SELECT ?s ?p ?o WHERE { ?s ?p ?o }");

    BROUWTOREN = valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE,
        "brewery/900e5c1c-d292-48c8-b9bd-1baf02ee2d2c");
    PERCENTAGES_INFORMATION_PRODUCT = valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE,
        "brewery/information/alcohol-percentages");
    ORIGIN_INFORMATION_PRODUCT =
        valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE, "brewery/information/origins");
  }

  private DBEERPEDIA() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", DBEERPEDIA.class));
  }

}
