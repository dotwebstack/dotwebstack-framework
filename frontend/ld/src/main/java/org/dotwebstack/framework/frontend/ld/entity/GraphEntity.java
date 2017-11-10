package org.dotwebstack.framework.frontend.ld.entity;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.GraphQueryResult;

public class GraphEntity extends AbstractEntity<GraphQueryResult> {

  public GraphEntity(@NonNull GraphQueryResult result, @NonNull Representation representation) {
    super(result, representation);
  }

}
