package org.dotwebstack.framework.frontend.ld.entity;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.QueryResult;

public class HtmlTupleEntity<R extends QueryResult<?>> extends AbstractEntity<R> {

  public HtmlTupleEntity(@NonNull R result, @NonNull Representation representation) {
    super(result, representation);
  }

}
