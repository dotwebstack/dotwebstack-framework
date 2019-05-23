package org.dotwebstack.framework.backend.rdf4j.directives;

public final class Rdf4jDirectives {

  public static final String SPARQL_NAME;

  public static final String SPARQL_ARG_REPOSITORY;

  public static final String SPARQL_ARG_SUBJECT;

  public static final String SPARQL_ARG_LIMIT;

  public static final String SPARQL_ARG_OFFSET;

  public static final String SPARQL_ARG_ORDER_BY;

  private Rdf4jDirectives() {}

  static {
    SPARQL_NAME = "sparql";
    SPARQL_ARG_REPOSITORY = "repository";
    SPARQL_ARG_SUBJECT = "subject";
    SPARQL_ARG_ORDER_BY = "orderBy";
    SPARQL_ARG_LIMIT = "limit";
    SPARQL_ARG_OFFSET = "offset";
  }

}
