package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dotwebstack.framework.backend.rdf4j.expression.ExpressionContext;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

@Getter
@Setter
@Builder
public class Vertice {

  private Variable subject;

  private Iri iri;

  private List<Edge> edges;

  private List<ExpressionContext> filters;

  public List<TriplePattern> getConstructPatterns() {
    if (!Objects.isNull(edges) && !edges.isEmpty()) {
      return edges.stream()
          .flatMap(edge -> edge.getConstructPatterns(subject)
              .stream())
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public List<GraphPattern> getWherePatterns() {
    List<GraphPattern> patterns = new ArrayList<>();
    if (!Objects.isNull(edges) && !edges.isEmpty()) {
      patterns.addAll(edges.stream()
          .flatMap(edge -> edge.getWherePatterns(subject)
              .stream())
          .filter(Objects::nonNull)
          .collect(Collectors.toList()));
    }

    return patterns;
  }
}
