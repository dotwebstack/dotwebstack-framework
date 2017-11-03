package org.dotwebstack.framework.frontend.ld.entity;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;


abstract class AbstractEntity<T> {

  private final T queryResult;

  private final Representation representation;

  public AbstractEntity(@NonNull T queryResult, @NonNull Representation representation) {
    this.queryResult = queryResult;
    this.representation = representation;
  }

  public T getQueryResult() {
    return queryResult;
  }

  public Representation getRepresentation() {
    return representation;
  }
}
