package org.dotwebstack.framework.backend.rdf4j.directives;

public final class Directives {

  public static final String SHAPE_NAME;

  public static final String SHAPE_ARG_URI;

  public static final String SHAPE_ARG_GRAPH;

  public static final String SUBJECT_NAME;

  public static final String SUBJECT_ARG_PREFIX;

  private Directives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Directives.class));
  }

  static {
    SHAPE_NAME = "shape";
    SHAPE_ARG_URI = "uri";
    SHAPE_ARG_GRAPH = "graph";
    SUBJECT_NAME = "subject";
    SUBJECT_ARG_PREFIX = "prefix";
  }

}
