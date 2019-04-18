package org.dotwebstack.framework.backend.rdf4j.directives;

public final class Rdf4jDirectives {

  public static final String SPARQL_NAME;

  public static final String SPARQL_ARG_REPOSITORY;

  public static final String SPARQL_ARG_SUBJECT;

  private Rdf4jDirectives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Rdf4jDirectives.class));
  }

  static {
    SPARQL_NAME = "sparql";
    SPARQL_ARG_REPOSITORY = "repository";
    SPARQL_ARG_SUBJECT = "subject";
  }

}
