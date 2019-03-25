package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

public final class Directives {

  public static final String SELECT_NAME;

  public static final String SELECT_ARG_BACKEND;

  public static final String SELECT_ARG_SUBJECT;

  public static final String SHAPE_NAME;

  public static final String SHAPE_ARG_BACKEND;

  public static final String SHAPE_ARG_URI;

  public static final String SHAPE_ARG_GRAPH;

  private Directives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Directives.class));
  }

  static {
    SELECT_NAME = "select";
    SELECT_ARG_BACKEND = "backend";
    SELECT_ARG_SUBJECT = "subject";
    SHAPE_NAME = "shape";
    SHAPE_ARG_BACKEND = "backend";
    SHAPE_ARG_URI = "uri";
    SHAPE_ARG_GRAPH = "graph";
  }

}
