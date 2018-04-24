package org.dotwebstack.framework.vocabulary;

import org.dotwebstack.framework.backend.ResultType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class ELMO {

  public static final IRI CONFIG_GRAPHNAME;

  public static final IRI SHACL_GRAPHNAME;

  public static final IRI SHACL_CONCEPT_GRAPHNAME;

  public static final IRI CONTAINS_PROP;

  public static final IRI INFORMATION_PRODUCT;

  public static final IRI INFORMATION_PRODUCT_PROP;

  public static final IRI TRANSACTION;

  public static final IRI TRANSACTION_REPOSITORY;

  public static final IRI TRANSACTION_PROP;

  public static final IRI SEQUENTIAL_FLOW_PROP;

  public static final IRI UNKNOWN_FLOW_PROP;

  public static final IRI STEP;

  public static final IRI PERSISTENCE_STEP;

  public static final IRI UPDATE_STEP;

  public static final IRI ASSERTION_STEP;

  public static final IRI ASSERT;

  public static final IRI ASSERT_NOT;

  public static final IRI VALIDATION_STEP;

  public static final IRI SITE;

  public static final IRI STAGE;

  public static final IRI STAGE_PROP;

  public static final IRI DOMAIN;

  public static final IRI BACKEND;

  public static final IRI SPARQL_BACKEND;

  public static final IRI USERNAME;

  public static final IRI PASSWORD;

  public static final IRI BASE_PATH;

  public static final IRI SITE_PROP;

  public static final IRI ENDPOINT;

  public static final IRI QUERY;

  public static final IRI BACKEND_PROP;

  public static final IRI REPRESENTATION;

  public static final IRI REDIRECTION;

  public static final IRI REDIRECT_TEMPLATE;

  public static final IRI APPEARANCE;

  public static final IRI RESOURCE_APPEARANCE;

  public static final IRI TABLE_APPEARANCE;

  public static final IRI APPEARANCE_PROP;

  public static final IRI PATH_PATTERN;

  public static final IRI URI_PARAMETER_MAPPER;

  public static final IRI PARAMETER_MAPPER_PROP;

  public static final IRI PARAMETER;

  public static final IRI TERM_PARAMETER;

  public static final IRI PAGE_PARAMETER;

  public static final IRI PAGE_SIZE_PARAMETER;

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

  public static final IRI LAYOUT;

  public static final IRI LAYOUT_PROP;

  public static final IRI APPLIES_TO_PROP;

  public static final IRI ENDPOINT_PROP;

  public static final IRI DYNAMIC_ENDPOINT;

  public static final IRI GET_REPRESENTATION_PROP;

  public static final IRI POST_REPRESENTATION_PROP;

  public static final IRI PERSISTENCE_STRATEGY_INSERT_INTO_GRAPH;

  public static final IRI PERSISTENCE_STRATEGY_UNKNOWN;

  public static final IRI PERSISTENCE_STRATEGY_PROP;

  public static final IRI TARGET_GRAPH_PROP;

  public static final IRI CONFORMS_TO_PROP;

  public static final IRI SERVICE;

  public static final IRI SERVICE_POST_PROP;

  public static final IRI SERVICE_PUT_PROP;

  public static final IRI SERVICE_DELETE_PROP;

  private static final String NAMESPACE_BASE = "http://dotwebstack.org/def/elmo";

  private static final String NAMESPACE = NAMESPACE_BASE + "#";

  private static final String NAMESPACE_EXTENSION_RESULTTYPE = NAMESPACE_BASE + "/resulttype/";

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    CONFIG_GRAPHNAME = valueFactory.createIRI(ELMO.NAMESPACE, "Config");

    SHACL_GRAPHNAME = valueFactory.createIRI(ELMO.NAMESPACE, "Shacl");

    SHACL_CONCEPT_GRAPHNAME = valueFactory.createIRI(ELMO.NAMESPACE, "ConceptShapeGraph");

    // Classes
    INFORMATION_PRODUCT = valueFactory.createIRI(ELMO.NAMESPACE, "InformationProduct");

    TRANSACTION = valueFactory.createIRI(ELMO.NAMESPACE, "Transaction");
    TRANSACTION_REPOSITORY = valueFactory.createIRI(ELMO.NAMESPACE, "TransactionRepository");
    STEP = valueFactory.createIRI(ELMO.NAMESPACE, "Step");
    PERSISTENCE_STEP = valueFactory.createIRI(ELMO.NAMESPACE, "PersistenceStep");
    VALIDATION_STEP = valueFactory.createIRI(ELMO.NAMESPACE, "ValidationStep");
    UPDATE_STEP = valueFactory.createIRI(ELMO.NAMESPACE, "UpdateStep");
    ASSERTION_STEP = valueFactory.createIRI(ELMO.NAMESPACE, "AssertionStep");

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

    LAYOUT = valueFactory.createIRI(ELMO.NAMESPACE, "Layout");
    ENDPOINT = valueFactory.createIRI(ELMO.NAMESPACE, "Endpoint");
    DYNAMIC_ENDPOINT = valueFactory.createIRI(ELMO.NAMESPACE, "DynamicEndpoint");

    // Properties
    BASE_PATH = valueFactory.createIRI(ELMO.NAMESPACE, "basePath");
    DOMAIN = valueFactory.createIRI(ELMO.NAMESPACE, "domain");
    SITE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "site");

    ENDPOINT_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "endpoint");
    QUERY = valueFactory.createIRI(ELMO.NAMESPACE, "query");
    RESULT_TYPE = valueFactory.createIRI(ELMO.NAMESPACE, "resultType");
    RESULT_TYPE_DEFAULT =
        valueFactory.createIRI(ELMO.NAMESPACE_EXTENSION_RESULTTYPE, ResultType.GRAPH.name());

    INFORMATION_PRODUCT_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "informationProduct");
    TRANSACTION_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "transaction");
    PATH_PATTERN = valueFactory.createIRI(ELMO.NAMESPACE, "pathPattern");
    PARAMETER_MAPPER_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "parameterMapper");
    BACKEND_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "backend");
    USERNAME = valueFactory.createIRI(ELMO.NAMESPACE, "username");
    PASSWORD = valueFactory.createIRI(ELMO.NAMESPACE, "password");
    STAGE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "stage");

    SEQUENTIAL_FLOW_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "sequentialFlow");
    UNKNOWN_FLOW_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "unknownFlow");

    ASSERT = valueFactory.createIRI(ELMO.NAMESPACE, "assert");
    ASSERT_NOT = valueFactory.createIRI(ELMO.NAMESPACE, "assertNot");

    TARGET_GRAPH_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "targetGraph");

    PERSISTENCE_STRATEGY_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "persistenceStrategy");

    PERSISTENCE_STRATEGY_INSERT_INTO_GRAPH =
        valueFactory.createIRI(ELMO.NAMESPACE_BASE, "/persistence-strategy/InsertIntoGraph");

    PERSISTENCE_STRATEGY_UNKNOWN =
        valueFactory.createIRI(ELMO.NAMESPACE_BASE, "/persistence-strategy/Unknown");

    REDIRECT_TEMPLATE = valueFactory.createIRI(ELMO.NAMESPACE, "redirectTemplate");

    SERVICE = valueFactory.createIRI(ELMO.NAMESPACE, "Service");

    PARAMETER = valueFactory.createIRI(ELMO.NAMESPACE, "Parameter");
    TERM_PARAMETER = valueFactory.createIRI(ELMO.NAMESPACE, "TermParameter");
    PAGE_PARAMETER = valueFactory.createIRI(ELMO.NAMESPACE, "PageParameter");
    PAGE_SIZE_PARAMETER = valueFactory.createIRI(ELMO.NAMESPACE, "PageSizeParameter");

    OPTIONAL_PARAMETER_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "optionalParameter");
    REQUIRED_PARAMETER_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "requiredParameter");

    NAME_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "name");
    APPEARANCE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "appearance");
    CONTAINS_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "contains");

    SOURCE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "source");
    PATTERN_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "pattern");
    TEMPLATE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "template");
    TARGET_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "target");

    LAYOUT_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "layout");
    SHAPE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "shape");

    APPLIES_TO_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "appliesTo");

    GET_REPRESENTATION_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "getRepresentation");
    POST_REPRESENTATION_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "postRepresentation");

    CONFORMS_TO_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "conformsTo");

    SERVICE_POST_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "postService");
    SERVICE_PUT_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "putService");
    SERVICE_DELETE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "deleteService");
  }

  private ELMO() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", ELMO.class));
  }

}
