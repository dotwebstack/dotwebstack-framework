package org.dotwebstack.framework.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public final class DBEERPEDIA {

  public static final String NAMESPACE = "http://dbeerpedia.org#";

  public static final Literal BASE_PATH;

  public static final IRI SHACL_CONCEPT_GRAPHNAME;

  public static final String TITLE = "Dbeerpedia";

  public static final IRI NAME;

  public static final IRI FOUNDATION;

  public static final IRI SINCE;

  public static final IRI PLACE;

  public static final IRI FTE;

  public static final String BREWERY_DAVO_NAME = "Davo Bieren Deventer";

  public static final Literal BREWERY_DAVO;

  public static final String OBJECT_NAMESPACE = "http://dbeerpedia.org/id/";

  public static final IRI PATH_PATTERN;

  public static final Literal DOMAIN;

  public static final Literal DOMAIN_NL;

  public static final IRI BACKEND;

  public static final IRI SECOND_BACKEND;

  public static final Literal USERNAME;

  public static final Literal PASSWORD;

  public static final Literal ENDPOINT;

  public static final IRI BREWERIES;

  public static final IRI TUPLE_BREWERIES;

  public static final IRI GRAPH_BREWERIES;

  public static final Literal BREWERIES_LABEL;

  public static final Literal WINERIES_LABEL;

  public static final Literal SELECT_ALL_QUERY;

  public static final Literal CONSTRUCT_ALL_QUERY;

  public static final Literal ASK_ALL_QUERY;

  public static final Literal ASK2_ALL_QUERY;

  public static final Literal MALFORMED_QUERY;

  public static final IRI BROUWTOREN;

  public static final Literal BROUWTOREN_NAME;

  public static final Literal BROUWTOREN_YEAR_OF_FOUNDATION;

  public static final Literal BROUWTOREN_DATE_OF_FOUNDATION;

  public static final Literal BROUWTOREN_PLACE;

  public static final Literal BROUWTOREN_CRAFT_MEMBER;

  public static final Literal BROUWTOREN_FTE;

  public static final Literal BROUWTOREN_LITERS_PER_YEAR;

  public static final Literal BROUWTOREN_HOP_USAGE_PER_YEAR;

  public static final IRI MAXIMUS;

  public static final Literal MAXIMUS_NAME;

  public static final Literal MAXIMUS_YEAR_OF_FOUNDATION;

  public static final Literal MAXIMUS_DATE_OF_FOUNDATION;

  public static final Literal MAXIMUS_PLACE;

  public static final Literal MAXIMUS_FTE;

  public static final IRI PERCENTAGES_INFORMATION_PRODUCT;

  public static final IRI ORIGIN_INFORMATION_PRODUCT;

  public static final IRI GRAPH_BREWERY_LIST_REPRESENTATION;

  public static final IRI TUPLE_BREWERY_LIST_REPRESENTATION;

  public static final IRI BREWERY_REPRESENTATION;

  public static final IRI ID2DOC_REDIRECTION;

  public static final IRI ID2DOC_DUMMY_REDIRECTION;

  public static final IRI SUBJECT_FROM_URL;

  public static final Literal SUBJECT_FROM_PATH_PATTERN;

  public static final Literal SUBJECT_FROM_URL_TEMPLATE;

  public static final String SUBJECT_PARAMETER_NAME = "SubjectParameter";

  public static final IRI SUBJECT_PARAMETER;

  public static final Literal ID2DOC_PATH_PATTERN;

  public static final Literal ID2DOC_REDIRECT_TEMPLATE;

  @java.lang.SuppressWarnings("squid:S1075")
  public static final String BREWERY_ID_PATH = "/id/brewery";

  @java.lang.SuppressWarnings("squid:S1075")
  public static final String BREWERY_DOC_PATH = "/doc/brewery";

  public static final IRI BREWERY_APPEARANCE;

  public static final IRI CUSTOM_APPEARANCE_PROP;

  public static final String OPENAPI_DESCRIPTION = "DBeerpedia API";

  public static final String OPENAPI_HOST = "dbpeerpedia.org";

  @java.lang.SuppressWarnings("squid:S1075")
  public static final String OPENAPI_BASE_PATH = "/api/v1";

  public static final String ORG_HOST = "dbeerpedia.org";

  public static final String NL_HOST = "dbeerpedia.nl";

  public static final String SYSTEM_GRAPH = "http://dbeerpedia.nl/configuration/Theatre";

  public static final String RESOURCE_PATH = "file:./config";

  public static final IRI SYSTEM_GRAPH_IRI;

  public static final IRI SITE;

  public static final IRI SITE_NL;

  public static final IRI STAGE;

  public static final IRI SECOND_STAGE;

  public static final String NAME_PARAMETER = "nameParameter";

  public static final IRI NAME_PARAMETER_ID;

  public static final Literal NAME_PARAMETER_VALUE;

  public static final String NAME_PARAMETER_VALUE_STRING;

  public static final IRI PLACE_PARAMETER_ID;

  public static final Literal PLACE_PARAMETER_VALUE;

  public static final String PLACE_PARAMETER_VALUE_STRING;

  @java.lang.SuppressWarnings("squid:S1075")
  public static final String PATH_PATTERN_VALUE = "/holyBeer";

  public static final IRI BREWERY_TYPE;

  public static final IRI WINERY_TYPE;

  public static final IRI LAYOUT;

  public static final IRI LAYOUT_NL;

  public static final Literal LAYOUT_VALUE;

  public static final Literal LAYOUT_NL_VALUE;

  public static final IRI APPLIES_TO;

  public static final IRI DOC_ENDPOINT;

  public static final IRI DEFAULT_ENDPOINT;

  public static final IRI PERSISTENCE_STEP;

  public static final IRI ASSERTION_IF_EXIST_STEP;

  public static final IRI ASSERTION_IF_NOT_EXIST_STEP;

  public static final IRI UPDATE_STEP;

  public static final IRI PRE_UPDATE_STEP;

  public static final IRI POST_UPDATE_STEP;

  public static final IRI VALIDATION_STEP;

  public static final IRI TRANSACTION;

  public static final IRI SEQUENTIAL_FLOW;

  public static final IRI SERVICE_POST;

  public static final IRI SERVICE_PUT;

  public static final IRI SERVICE_DELETE;

  public static final String RML_MAPPING_NAME = DBEERPEDIA.NAMESPACE + "RmlMapping";

  public static final IRI RML_MAPPING;

  public static final IRI RML_MAPPING2;

  public static final Literal ASK_ALL_QUERY_SERVICE_TAG;

  public static final Literal BROUWTOREN_DATETIME_OF_FIRST_BEER;

  public static final IRI FIRSTBEER;

  public static final Literal UPDATE_QUERY_SERVICE_TAG;

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    SHACL_CONCEPT_GRAPHNAME = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "ConceptShapeGraph");
    SYSTEM_GRAPH_IRI = valueFactory.createIRI(DBEERPEDIA.SYSTEM_GRAPH);
    SITE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Site");
    STAGE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Stage");
    SECOND_STAGE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "SecondStage");
    DOMAIN = valueFactory.createLiteral(ORG_HOST);
    BASE_PATH = valueFactory.createLiteral("/special");

    NAME_PARAMETER_ID = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, NAME_PARAMETER);
    NAME_PARAMETER_VALUE_STRING = "name";
    NAME_PARAMETER_VALUE = valueFactory.createLiteral(NAME_PARAMETER_VALUE_STRING);

    PLACE_PARAMETER_ID = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "placeParameter");
    PLACE_PARAMETER_VALUE_STRING = "place";
    PLACE_PARAMETER_VALUE = valueFactory.createLiteral(PLACE_PARAMETER_VALUE_STRING);

    FOUNDATION = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Foundation");
    NAME = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Name");
    SINCE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Since");
    PLACE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Place");
    FTE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "FTE");

    BREWERY_DAVO = valueFactory.createLiteral(BREWERY_DAVO_NAME);

    SITE_NL = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "SiteNL");
    DOMAIN_NL = valueFactory.createLiteral(NL_HOST);

    BACKEND = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Backend");
    SECOND_BACKEND = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "SecondBackend");
    ENDPOINT = valueFactory.createLiteral("http://localhost:8080/sparql", XMLSchema.ANYURI);
    USERNAME = valueFactory.createLiteral("john_doe", XMLSchema.STRING);
    PASSWORD = valueFactory.createLiteral("supersecret", XMLSchema.STRING);

    BREWERIES = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Breweries");
    TUPLE_BREWERIES = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "TupleBreweries");
    GRAPH_BREWERIES = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "GraphBreweries");
    BREWERIES_LABEL = valueFactory.createLiteral("Beer breweries in The Netherlands");
    WINERIES_LABEL = valueFactory.createLiteral("Wineries in The Netherlands");
    MALFORMED_QUERY = valueFactory.createLiteral("CONSTRUCT ?s ?p ?o WHERE { ?s ?p ?o }");
    SELECT_ALL_QUERY = valueFactory.createLiteral("SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
    CONSTRUCT_ALL_QUERY = valueFactory.createLiteral("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
    ASK_ALL_QUERY = valueFactory.createLiteral("ASK WHERE { ?s ?p ?o }");
    ASK2_ALL_QUERY = valueFactory.createLiteral("ASK WHERE { ?s a ?o }");
    ASK_ALL_QUERY_SERVICE_TAG = valueFactory.createLiteral(
        "PREFIX dbeerpedia: <http://dbeerpedia.org#> ASK { ?s ?p ?o SERVICE "
            + "dbeerpedia:Backend { ?s ?p ?o } }");
    UPDATE_QUERY_SERVICE_TAG = valueFactory.createLiteral("PREFIX dbeerpedia: <http://dbeerpedia"
        + ".org#> insert { ?concept rdfs:label ?label } where { ?s ?p ?o SERVICE "
        + "dbeerpedia:Backend { ?s rdfs:label ?p. } }");



    BROUWTOREN = valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE,
        "brewery/900e5c1c-d292-48c8-b9bd-1baf02ee2d2c");
    BROUWTOREN_NAME = valueFactory.createLiteral("Brouwtoren");
    BROUWTOREN_YEAR_OF_FOUNDATION = valueFactory.createLiteral(2014);
    BROUWTOREN_DATE_OF_FOUNDATION =
        valueFactory.createLiteral(createDate(2014, 1, 1).toString(), XMLSchema.DATE);
    BROUWTOREN_CRAFT_MEMBER = valueFactory.createLiteral(true);
    BROUWTOREN_FTE = valueFactory.createLiteral(1.8);
    BROUWTOREN_HOP_USAGE_PER_YEAR = valueFactory.createLiteral(8.8f);
    BROUWTOREN_LITERS_PER_YEAR = valueFactory.createLiteral(Long.MAX_VALUE);
    BROUWTOREN_PLACE = valueFactory.createLiteral("Nijmegen");

    FIRSTBEER = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "FirstBeer");
    BROUWTOREN_DATETIME_OF_FIRST_BEER = valueFactory.createLiteral(
        ZonedDateTime.of(LocalDateTime.of(LocalDate.of(2012, 12, 25), LocalTime.of(12, 12, 12)),
            ZoneId.systemDefault()).toString(),
        XMLSchema.DATETIME);

    MAXIMUS = valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE,
        "brewery/0c0d7df2-a830-11e7-abc4-cec278b6b50a");
    MAXIMUS_NAME = valueFactory.createLiteral("Maximus");
    MAXIMUS_YEAR_OF_FOUNDATION = valueFactory.createLiteral(2012);
    MAXIMUS_DATE_OF_FOUNDATION =
        valueFactory.createLiteral(createDate(2012, 9, 27).toString(), XMLSchema.DATE);
    MAXIMUS_FTE = valueFactory.createLiteral(2.4);
    MAXIMUS_PLACE = valueFactory.createLiteral("Utrecht");

    LAYOUT_VALUE = valueFactory.createLiteral(DBEERPEDIA.NAMESPACE, "stage-layout.css.css");

    LAYOUT_NL_VALUE = valueFactory.createLiteral(DBEERPEDIA.NAMESPACE, "stage-nl-layout.css.css");

    PERCENTAGES_INFORMATION_PRODUCT = valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE,
        "brewery/information/alcohol-percentages");
    ORIGIN_INFORMATION_PRODUCT =
        valueFactory.createIRI(DBEERPEDIA.OBJECT_NAMESPACE, "brewery/information/origins");

    GRAPH_BREWERY_LIST_REPRESENTATION =
        valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "GraphBreweryListRepresentation");

    TUPLE_BREWERY_LIST_REPRESENTATION =
        valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "TupleBreweryListRepresentation");

    BREWERY_REPRESENTATION = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "BreweryRepresentation");

    ID2DOC_REDIRECTION = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Id2doc");

    ID2DOC_DUMMY_REDIRECTION = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Id2docDummy");

    ID2DOC_PATH_PATTERN = valueFactory.createLiteral("/id/{resource}");

    ID2DOC_REDIRECT_TEMPLATE = valueFactory.createLiteral("/doc/{resource}");

    SUBJECT_FROM_URL = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "SubjectFromUrl");

    SUBJECT_FROM_PATH_PATTERN = valueFactory.createLiteral("http://{domain}/doc/{reference}");

    SUBJECT_FROM_URL_TEMPLATE = valueFactory.createLiteral("http://dbeerpedia.org/id/{reference}");

    SUBJECT_PARAMETER = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, SUBJECT_PARAMETER_NAME);

    BREWERY_APPEARANCE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "BreweryAppearance");

    CUSTOM_APPEARANCE_PROP = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "customAppearanceProp");

    PATH_PATTERN = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "pathPattern");

    BREWERY_TYPE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Brewery");

    WINERY_TYPE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "Winery");

    LAYOUT = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "BeerLayout");

    LAYOUT_NL = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "BeerLayoutNL");

    APPLIES_TO = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "appliesTo");

    DOC_ENDPOINT = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "DocEndpoint");

    DEFAULT_ENDPOINT = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "DefaultEndpoint");

    PERSISTENCE_STEP = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "PersistenceStep");

    ASSERTION_IF_EXIST_STEP = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "AssertIfExists");

    ASSERTION_IF_NOT_EXIST_STEP = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "AssertIfNotExists");

    UPDATE_STEP = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "UpdateStep");

    PRE_UPDATE_STEP = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "PreUpdateStep");

    POST_UPDATE_STEP = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "PostUpdateStep");

    TRANSACTION = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "InsertConcept");

    RML_MAPPING = valueFactory.createIRI(RML_MAPPING_NAME);

    RML_MAPPING2 = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "RmlMapping2");

    SEQUENTIAL_FLOW = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "");

    VALIDATION_STEP = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "ValidationStep");

    SERVICE_DELETE = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "deleteService");
    SERVICE_POST = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "postService");
    SERVICE_PUT = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "putService");
  }

  private DBEERPEDIA() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", DBEERPEDIA.class));
  }

  private static LocalDate createDate(int year, int month, int day) {
    return LocalDate.of(year, month, day);
  }

}
