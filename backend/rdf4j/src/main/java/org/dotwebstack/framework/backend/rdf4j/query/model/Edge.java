package org.dotwebstack.framework.backend.rdf4j.query.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;


@Data
@Builder
public class Edge implements Comparable<Edge> {

  private PropertyShape propertyShape;

  private RdfPredicate predicate;

  private RdfPredicate constructPredicate;

  private Vertice object;

  private Aggregate aggregate;

  @Builder.Default
  private List<PathType> pathTypes = new ArrayList<>();

  public RdfPredicate getConstructPredicate() {
    return Objects.nonNull(constructPredicate) ? constructPredicate : predicate;
  }

  @Override
  public int compareTo(Edge other) {
    return Boolean.compare(this.isOptional(), other.isOptional());
  }

  public String toString() {
    return predicate.getQueryString() + " " + object.getSubject()
        .getQueryString();
  }

  public boolean hasReusablePaths() {
    return pathTypes.stream()
        .anyMatch(PathType::hasReusablePaths);
  }

  public void addPathType(PathType pathType) {
    if (!pathTypes.contains(pathType)) {
      pathTypes.add(pathType);
    }
  }

  public boolean isVisible() {
    return pathTypes.stream()
        .anyMatch(PathType::isVisible);
  }

  public boolean isOptional() {
    return !this.isRequired();
  }

  public boolean isRequired() {
    return pathTypes.stream()
        .anyMatch(PathType::isRequired);
  }
}
