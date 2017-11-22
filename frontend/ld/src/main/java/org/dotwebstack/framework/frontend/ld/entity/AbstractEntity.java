package org.dotwebstack.framework.frontend.ld.entity;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.QueryResult;


public abstract class AbstractEntity<R extends QueryResult<?>> implements Entity<R> {

  private final R queryResult;

  private final Representation representation;

  AbstractEntity(@NonNull R queryResult, @NonNull Representation representation) {
    this.queryResult = queryResult;
    this.representation = representation;
  }

  public R getQueryResult() {
    return queryResult;
  }

  public Representation getRepresentation() {
    return representation;
  }
}
