package org.dotwebstack.framework.backend.rdf4j.query.model;

public enum PathType {

  SELECTED_FIELD(true, true, false), // this path is used in the selection set
  FILTER(true, false, true), // this path is used in a single filter
  NESTED_FILTER(false, false, true), // this path is used for a nested filter
  SORT(true, false, false), // used for sorting
  CONSTRAINT(true, true, true); // used for constraints

  private boolean reusablePaths;

  private boolean visibleInConstruct;

  private boolean required;

  PathType(boolean reusablePaths, boolean visibleInConstruct, boolean required) {
    this.reusablePaths = reusablePaths;
    this.visibleInConstruct = visibleInConstruct;
    this.required = required;
  }

  public boolean hasReusablePaths() {
    return reusablePaths;
  }

  public boolean isVisible() {
    return this.visibleInConstruct;
  }

  public boolean isRequired() {
    return this.required;
  }
}
