package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.sparql.query.QueryStringUtil;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;

@Getter
@RequiredArgsConstructor
class GraphPatternWithValues implements GraphPattern {

  private final GraphPattern graphPattern;

  private final Map<Variable, Set<? extends Value>> valuesMap;

  @Override
  public String getQueryString() {
    var valuesPrefix = valuesMap.entrySet()
        .stream()
        .map(entry -> valueExpr(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining());

    return valuesPrefix.concat(graphPattern.getQueryString());
  }

  private String valueExpr(Variable variable, Set<? extends Value> values) {
    return String.format("VALUES %s {%s}", variable.getQueryString(), values.stream()
        .map(QueryStringUtil::valueToString)
        .collect(Collectors.joining(" ")))
        .concat("\n");
  }
}
