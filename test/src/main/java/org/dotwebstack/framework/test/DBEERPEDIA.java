package org.dotwebstack.framework.test;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public final class DBEERPEDIA {


  public static final String NAMESPACE = "http://dbeerpedia.org#";

  public static final Literal BASE_PATH;

  public static final IRI NAME;

  public static final String BREWERY_DAVO_NAME = "Davo Bieren Deventer";

  public static final Literal BREWERY_DAVO;

  public static final String OBJECT_NAMESPACE = "http://dbeerpedia.org/id/";

  public static final IRI URL_PATTERN;

  public static final Literal DOMAIN;

  public static final Literal DOMAIN_NL;

  public static final IRI BACKEND;

  public static final IRI SECOND_BACKEND;

  public static final Literal ENDPOINT;

  public static final IRI BREWERIES;

  public static final Literal BREWERIES_LABEL;

  public static final Literal WINERIES_LABEL;

  public static final Literal SELECT_ALL_QUERY;

  public static final IRI BROUWTOREN;

  public static final Literal BROUWTOREN_NAME;

  public static final Literal BROUWTOREN_YEAR_OF_FOUNDATION;

  public static final Literal BROUWTOREN_CRAFT_MEMBER;

  public static final Literal BROUWTOREN_FTE;

  public static final IRI PERCENTAGES_INFORMATION_PRODUCT;

  public static final IRI ORIGIN_INFORMATION_PRODUCT;

  public static final IRI BREWERY_LIST_REPRESENTATION;

  public static final IRI BREWERY_REPRESENTATION;

  public static final String OPENAPI_DESCRIPTION = "DBeerpedia API";

  public static final String OPENAPI_HOST = "dbpeerpedia.org";

  @java.lang.SuppressWarnings("squid:S1075")
  public static final String OPENAPI_BASE_PATH = "/api/v1";

  public static final String ORG_HOST = "dbeerpedia.org";

  public static final String NL_HOST = "dbeerpedia.nl";

  public static final IRI SITE;

  public static final IRI SITE_NL;

  public static final IRI STAGE;

  public static final IRI SECOND_STAGE;

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    SITE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Site");
    STAGE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Stage");
    SECOND_STAGE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "SecondStage");
    DOMAIN = valueFactory.createLiteral("dbeerpedia.org");
    BASE_PATH = valueFactory.createLiteral("/special");

    NAME = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Name");
    BREWERY_DAVO = valueFactory.createLiteral(BREWERY_DAVO_NAME);

    SITE_NL = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "SiteNL");
    DOMAIN_NL = valueFactory.createLiteral("dbeerpedia.nl");

    BACKEND = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Backend");
    SECOND_BACKEND = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "SecondBackend");
    ENDPOINT = valueFactory.createLiteral("http://localhost:8080/sparql", XMLSchema.ANYURI);

    BREWERIES = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Breweries");
    BREWERIES_LABEL = valueFactory.createLiteral("Beer breweries in The Netherlands");
    WINERIES_LABEL = valueFactory.createLiteral("Wineries in The Netherlands");
    SELECT_ALL_QUERY = valueFactory.createLiteral("SELECT ?s ?p ?o WHERE { ?s ?p ?o }");

    BROUWTOREN = valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE,
        "brewery/900e5c1c-d292-48c8-b9bd-1baf02ee2d2c");
    BROUWTOREN_NAME = valueFactory.createLiteral("Brouwtoren");
    BROUWTOREN_YEAR_OF_FOUNDATION = valueFactory.createLiteral(2014);
    BROUWTOREN_CRAFT_MEMBER = valueFactory.createLiteral(true);
    BROUWTOREN_FTE = valueFactory.createLiteral(1.8);

    PERCENTAGES_INFORMATION_PRODUCT = valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE,
        "brewery/information/alcohol-percentages");
    ORIGIN_INFORMATION_PRODUCT =
        valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE, "brewery/information/origins");

    BREWERY_LIST_REPRESENTATION =
        valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "BreweryListRepresentation");

    BREWERY_REPRESENTATION = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "BreweryRepresentation");

    URL_PATTERN = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "urlPattern");
  }

  private DBEERPEDIA() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", DBEERPEDIA.class));
  }

}
