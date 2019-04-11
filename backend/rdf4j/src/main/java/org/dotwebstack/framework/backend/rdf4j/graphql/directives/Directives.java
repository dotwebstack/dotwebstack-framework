package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

public final class Directives {

  public static final String SPARQL_NAME;

  public static final String SPARQL_ARG_REPOSITORY;

  public static final String SPARQL_ARG_SUBJECT;

  private Directives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Directives.class));
  }

  static {
    SPARQL_NAME = "sparql";
    SPARQL_ARG_REPOSITORY = "repository";
    SPARQL_ARG_SUBJECT = "subject";
  }

}
