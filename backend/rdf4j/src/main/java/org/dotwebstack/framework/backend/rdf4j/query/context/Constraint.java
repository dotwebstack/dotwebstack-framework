package org.dotwebstack.framework.backend.rdf4j.query.context;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Data
@Builder
public class Constraint {

  private ConstraintType constraintType;

  private RdfPredicate predicate;

  private boolean isOptional;

  @Builder.Default
  private Set<Object> values = new HashSet<>();

  @Override
  public String toString() {
    return predicate.getQueryString() + " " + values.stream()
        .map(value -> {
          if (value instanceof Value) {
            return ((Value) value).stringValue();
          } else if (value instanceof Variable) {
            return ((Variable) value).getQueryString();
          }
          return value.toString();
        })
        .collect(Collectors.joining(", "));
  }
}
