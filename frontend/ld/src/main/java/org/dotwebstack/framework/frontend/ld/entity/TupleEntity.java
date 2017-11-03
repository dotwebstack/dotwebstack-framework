package org.dotwebstack.framework.frontend.ld.entity;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.TupleQueryResult;

public class TupleEntity extends AbstractEntity<TupleQueryResult> {

  public TupleEntity(@NonNull TupleQueryResult result, @NonNull Representation representation) {
    super(result, representation);
  }

}
