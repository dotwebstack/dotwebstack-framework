package org.dotwebstack.framework.backend.rdf4j;

public final class Directives {

  public static final String SUBJECT_NAME;

  public static final String SUBJECT_ARG_PREFIX;

  private Directives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Directives.class));
  }

  static {
    SUBJECT_NAME = "subject";
    SUBJECT_ARG_PREFIX = "prefix";
  }

}
