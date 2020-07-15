package org.dotwebstack.framework.backend.rdf4j.query.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;


@Data
@Builder
public class Vertice {

  private NodeShape nodeShape;

  private Variable subject;

  @Builder.Default
  private Set<Constraint> constraints = new HashSet<>();

  @Builder.Default
  private List<Edge> edges = new ArrayList<>();

  @Builder.Default
  private List<Filter> filters = new ArrayList<>();

  @Builder.Default
  private List<Orderable> orderables = new ArrayList<>();

  public Set<Constraint> getConstraints(ConstraintType constraintType) {
    return constraints.stream()
        .filter(constraint -> constraintType.equals(constraint.getConstraintType()))
        .collect(Collectors.toSet());
  }

  public void addFilter(Filter filter) {
    filters.add(filter);
  }
}
