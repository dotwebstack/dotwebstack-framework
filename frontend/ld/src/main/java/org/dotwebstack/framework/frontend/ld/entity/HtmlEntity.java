package org.dotwebstack.framework.frontend.ld.entity;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.result.HtmlResult;
import org.eclipse.rdf4j.query.GraphQueryResult;

public class HtmlEntity<R> extends AbstractEntity<HtmlResult<R>> {

  public HtmlEntity(@NonNull HtmlResult result, @NonNull Representation representation) {
    super(result, representation);
  }

}
