package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class ELMO {

  public static final String NAMESPACE = "http://dotwebstack.org/def/elmo#";

  public static final IRI INFORMATION_PRODUCT;

  public static final IRI SITE;

  public static final IRI STAGE;

  public static final IRI DOMAIN;

  public static final IRI BACKEND;

  public static final IRI SPARQL_BACKEND;

  public static final IRI BASE_PATH;

  public static final IRI SITE_PROP;

  public static final IRI ENDPOINT;

  public static final IRI QUERY;

  public static final IRI BACKEND_PROP;

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    // Classes
    INFORMATION_PRODUCT = valueFactory.createIRI(ELMO.NAMESPACE, "InformationProduct");

    SITE = valueFactory.createIRI(ELMO.NAMESPACE, "Site");
    STAGE = valueFactory.createIRI(ELMO.NAMESPACE, "Stage");

    BACKEND = valueFactory.createIRI(ELMO.NAMESPACE, "Backend");
    SPARQL_BACKEND = valueFactory.createIRI(ELMO.NAMESPACE, "SparqlBackend");

    // Properties
    BASE_PATH = valueFactory.createIRI(ELMO.NAMESPACE, "basePath");
    DOMAIN = valueFactory.createIRI(ELMO.NAMESPACE, "domain");
    SITE_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "site");

    ENDPOINT = valueFactory.createIRI(ELMO.NAMESPACE, "endpoint");
    QUERY = valueFactory.createIRI(ELMO.NAMESPACE, "query");

    BACKEND_PROP = valueFactory.createIRI(ELMO.NAMESPACE, "backend");
  }

  private ELMO() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", ELMO.class));
  }

}
