package org.dotwebstack.framework.backend.rdf4j.query.model;

public enum PathType {

  SELECTED_FIELD(true), // this path is used in the selection set
  FILTER(true), // this path is used in a single filter
  NESTED_FILTER(false), // this path is used for a nested filter
  SORT(true), // used for sorting
  CONSTRAINT(true); // used for constraints

  private boolean reusePaths;

  PathType(boolean reusePaths) {
    this.reusePaths = reusePaths;
  }

  public boolean isReusePaths() {
    return reusePaths;
  }
}
