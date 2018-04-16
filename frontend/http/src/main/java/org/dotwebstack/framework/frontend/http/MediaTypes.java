package org.dotwebstack.framework.frontend.http;

import javax.ws.rs.core.MediaType;

public class MediaTypes {

  public static final String LDJSON = "application/ld+json";

  public static final MediaType LDJSON_TYPE = MediaType.valueOf(LDJSON);

  public static final String TURTLE = "text/turtle";

  public static final MediaType TURTLE_TYPE = MediaType.valueOf(TURTLE);

  public static final String TRIG = "application/trig";

  public static final MediaType TRIG_TYPE = MediaType.valueOf(TRIG);

  public static final String RDFXML = "application/rdf+xml";

  public static final String APPLICATION_PROBLEM_JSON = "application/problem+json";

  public static final MediaType PROBLEM_JSON = MediaType.valueOf(APPLICATION_PROBLEM_JSON);

  public static final MediaType RDFXML_TYPE = MediaType.valueOf(RDFXML);

  public static final String SPARQL_RESULTS_JSON = "application/sparql-results+json";

  public static final MediaType SPARQL_RESULTS_JSON_TYPE = MediaType.valueOf(SPARQL_RESULTS_JSON);

  public static final String SPARQL_RESULTS_XML = "application/sparql-results+xml";

  public static final MediaType SPARQL_RESULTS_XML_TYPE = MediaType.valueOf(SPARQL_RESULTS_XML);

  private MediaTypes() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", MediaTypes.class));
  }

}
