package org.dotwebstack.framework.backend.rdf4j.query.context;

import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;


@Data
@Builder
class Edge implements Comparable<Edge> {

  private PropertyShape propertyShape;

  private RdfPredicate predicate;

  private RdfPredicate constructPredicate;

  private Vertice object;

  private boolean isOptional;

  private boolean isVisible;

  private Aggregate aggregate;

  RdfPredicate getConstructPredicate() {
    return Objects.nonNull(constructPredicate) ? constructPredicate : predicate;
  }

  @Override
  public int compareTo(Edge other) {
    return Boolean.compare(isOptional, other.isOptional);
  }

  public String toString() {
    return predicate.getQueryString() + " " + object.getSubject()
        .getQueryString();
  }

}
