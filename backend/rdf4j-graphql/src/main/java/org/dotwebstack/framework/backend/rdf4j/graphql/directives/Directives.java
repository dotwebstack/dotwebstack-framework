package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

public final class Directives {

  public static final String SPARQL_NAME;

  public static final String SPARQL_ARG_BACKEND;

  public static final String SPARQL_ARG_SUBJECT;

  public static final String SHACL_NAME;

  public static final String SHACL_ARG_SHAPE;

  private Directives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Directives.class));
  }

  static {
    SPARQL_NAME = "sparql";
    SPARQL_ARG_BACKEND = "backend";
    SPARQL_ARG_SUBJECT = "subject";
    SHACL_NAME = "shacl";
    SHACL_ARG_SHAPE = "shape";
  }

}
