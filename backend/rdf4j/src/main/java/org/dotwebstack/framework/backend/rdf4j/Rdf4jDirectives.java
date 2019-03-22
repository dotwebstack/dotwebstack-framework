package org.dotwebstack.framework.backend.rdf4j;

public final class Rdf4jDirectives {

  public static final String SUBJECT_NAME;

  public static final String SUBJECT_ARG_PREFIX;

  private Rdf4jDirectives() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Rdf4jDirectives.class));
  }

  static {
    SUBJECT_NAME = "subject";
    SUBJECT_ARG_PREFIX = "prefix";
  }

}
