package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Builder
@Getter
@Setter
public class SequencePath implements PropertyPath {

  private final PropertyPath first;

  private final PropertyPath rest;

  @Override
  public Set<Value> resolvePath(Model model, Resource subject, boolean inversed) {
    PropertyPath usedPath = (inversed && !PropertyPathHelper.isNil(rest)) ? this.rest : this.first;

    return usedPath.resolvePath(model, subject, inversed)
        .stream()
        .map(value -> resolveRest(model, value, inversed))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  private Set<Value> resolveRest(Model model, Value value, boolean inversed) {
    PropertyPath usedPath = inversed ? this.first : this.rest;

    // this is the last iteration, the rest is nonsense, return current value
    if (PropertyPathHelper.isNil(this.rest)) {
      return Collections.singleton(value);
    }

    // not the latest in the tree, dive one level deeper
    if (value instanceof BNode) {
      return usedPath.resolvePath(model, (Resource) value, inversed);
    }

    // Intermediate result has to be a blank node
    return Collections.emptySet();
  }

  @Override
  public RdfPredicate toPredicate() {
    return () -> {
      StringBuilder sb = new StringBuilder();
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
