package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Builder
@Getter
@Setter
public class ZeroOrOnePath extends BasePath {

  private final PredicatePath object;

  @Override
  public RdfPredicate toPredicate() {
    return () -> "(" + object.toPredicate()
        .getQueryString() + ")?";
  }
}
