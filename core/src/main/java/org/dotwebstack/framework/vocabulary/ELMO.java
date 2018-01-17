package org.dotwebstack.framework.vocabulary;

import org.dotwebstack.framework.backend.ResultType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class ELMO {

  public static final IRI CONFIG_GRAPHNAME;

  public static final IRI CONTAINS_PROP;

  public static final IRI INFORMATION_PRODUCT;

  public static final IRI INFORMATION_PRODUCT_PROP;

  public static final IRI SITE;

  public static final IRI STAGE;

  public static final IRI STAGE_PROP;

  public static final IRI DOMAIN;

  public static final IRI BACKEND;

  public static final IRI SPARQL_BACKEND;

  public static final IRI BASE_PATH;

  public static final IRI SITE_PROP;

  public static final IRI ENDPOINT;

  public static final IRI QUERY;

  public static final IRI BACKEND_PROP;

  public static final IRI REPRESENTATION;

  public static final IRI REDIRECTION;

  public static final IRI TARGET_URL;

  public static final IRI REDIRECTION_TEMPLATE;

  public static final IRI APPEARANCE;

  public static final IRI RESOURCE_APPEARANCE;

  public static final IRI TABLE_APPEARANCE;

  public static final IRI APPEARANCE_PROP;

  public static final IRI URL_PATTERN;

  public static final IRI URI_PARAMETER_MAPPER;

  public static final IRI PARAMETER_MAPPER_PROP;

  public static final IRI PARAMETER;

  public static final IRI TERM_FILTER;

  public static final IRI OPTIONAL_PARAMETER_PROP;

  public static final IRI REQUIRED_PARAMETER_PROP;

  public static final IRI NAME_PROP;

  public static final IRI SOURCE_PROP;

  public static final IRI PATTERN_PROP;

  public static final IRI TEMPLATE_PROP;

  public static final IRI TARGET_PROP;

  public static final IRI SHAPE_PROP;

  public static final IRI RESULT_TYPE;

  public static final IRI RESULT_TYPE_DEFAULT;

  private static final String NAMESPACE_BASE = "http://dotwebstack.org/def/elmo";

  private static final String NAMESPACE = NAMESPACE_BASE + "#";

  private static final String NAMESPACE_EXTENSION_RESULTTYPE = NAMESPACE_BASE + "/resulttype/";

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    CONFIG_GRAPHNAME = valueFactory.createIRI(ELMO.NAMESPACE, "Config");

    // Classes
    INFORMATION_PRODUCT = valueFactory.createIRI(ELMO.NAMESPACE, "InformationProduct");

    SITE = valueFactory.createIRI(ELMO.NAMESPACE, "Site");
    STAGE = valueFactory.createIRI(ELMO.NAMESPACE, "Stage");

    BACKEND = valueFactory.createIRI(ELMO.NAMESPACE, "Backend");
    SPARQL_BACKEND = valueFactory.createIRI(ELMO.NAMESPACE, "SparqlBackend");

    REPRESENTATION = valueFactory.createIRI(ELMO.NAMESPACE, "Representation");
    REDIRECTION = valueFactory.createIRI(ELMO.NAMESPACE, "Redirection");

    APPEARANCE = valueFactory.createIRI(ELMO.NAMESPACE, "Appearance");
    RESOURCE_APPEARANCE = valueFactory.createIRI(ELMO.NAMESPACE, "ResourceAppearance");
    TABLE_APPEARANCE = valueFactory.createIRI(ELMO.NAMESPACE, "TableAppearance");

    URI_PARAMETER_MAPPER = valueFactory.createIRI(ELMO.NAMESPACE, "UriParameterMapper");

    // Properties
    BASE_PATH = valueFactory.createIRI(ELMO.NAMESPACE, "basePath");
    DOMAIN = valueFactory.createIRI(ELMO.NAMESPACE, "domain");
    SITE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "site");

    ENDPOINT = valueFactory.createIRI(ELMO.NAMESPACE, "endpoint");
    QUERY = valueFactory.createIRI(ELMO.NAMESPACE, "query");
    RESULT_TYPE = valueFactory.createIRI(ELMO.NAMESPACE, "resultType");
    RESULT_TYPE_DEFAULT =
        valueFactory.createIRI(ELMO.NAMESPACE_EXTENSION_RESULTTYPE, ResultType.GRAPH.name());

    INFORMATION_PRODUCT_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "informationProduct");
    URL_PATTERN = valueFactory.createIRI(ELMO.NAMESPACE, "urlPattern");
    PARAMETER_MAPPER_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "parameterMapper");
    BACKEND_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "backend");
    STAGE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "stage");

    TARGET_URL = valueFactory.createIRI(ELMO.NAMESPACE, "targetUrl");
    REDIRECTION_TEMPLATE = valueFactory.createIRI(ELMO.NAMESPACE, "redirectTemplate");

    PARAMETER = valueFactory.createIRI(ELMO.NAMESPACE, "Parameter");
    TERM_FILTER = valueFactory.createIRI(ELMO.NAMESPACE, "TermFilter");

    OPTIONAL_PARAMETER_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "optionalParameter");
    REQUIRED_PARAMETER_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "requiredParameter");

    NAME_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "name");
    APPEARANCE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "appearance");
    CONTAINS_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "contains");

    SOURCE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "source");
    PATTERN_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "pattern");
    TEMPLATE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "template");
    TARGET_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "target");
    SHAPE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "shape");
  }

  private ELMO() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", ELMO.class));
  }

}
