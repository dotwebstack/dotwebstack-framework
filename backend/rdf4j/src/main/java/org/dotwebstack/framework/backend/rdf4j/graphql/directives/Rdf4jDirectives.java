package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Rdf4jDirectives {

  public static final String SPARQL_NAME;

  public static final String SPARQL_ARG_REPOSITORY;

  public static final String SPARQL_ARG_SUBJECT;

  static {
    SPARQL_NAME = "sparql";
    SPARQL_ARG_REPOSITORY = "repository";
    SPARQL_ARG_SUBJECT = "subject";
  }

}
