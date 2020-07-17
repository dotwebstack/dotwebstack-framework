package org.dotwebstack.framework.backend.rdf4j.query.model;

public enum PathType {

  SELECTED_FIELD(true, true, false), // this path is used in the selection set
  FILTER(true, false, true), // this path is used in a single filter
  NESTED_FILTER(false, false, true), // this path is used for a nested filter
  SORT(true, false, false), // used for sorting
  CONSTRAINT(true, true, true); // used for constraints

  private boolean reusePaths;

  private boolean visible;

  private boolean required;

  PathType(boolean reusePaths, boolean visible, boolean required) {
    this.reusePaths = reusePaths;
    this.visible = visible;
    this.required = required;
  }

  public boolean isReusePaths() {
    return reusePaths;
  }

  public boolean isVisible() {
    return this.visible;
  }

  public boolean isRequired() {
    return this.required;
  }
}
