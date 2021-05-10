package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Builder
@Getter
@Setter
public class SequencePath extends BasePath {

  private final PropertyPath first;

  private final PropertyPath rest;

  @Override
  public RdfPredicate toPredicate() {
    return () -> {
      var sb = new StringBuilder();
      sb.append(first.toPredicate()
          .getQueryString());

      if (!(PropertyPathHelper.isNil(rest))) {
        sb.append("/")
            .append(rest.toPredicate()
                .getQueryString());
      }

      return sb.toString();
    };
  }
}
