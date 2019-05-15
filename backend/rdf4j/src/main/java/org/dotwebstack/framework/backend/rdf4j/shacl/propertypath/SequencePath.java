package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Collections;
import java.util.Optional;
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

    return this.first.resolvePath(model, subject, inversed)
        .stream()
        .map(value -> resolveRest(model, value, inversed))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  private Set<Value> resolveRest(Model model, Value value, Boolean inversed) {
    if (!PropertyPathHelper.isNil(rest) && value instanceof BNode) {
      return rest.resolvePath(model, (Resource) value, inversed);
    }
    return Collections.singleton(value);
  }

  @Override
  public RdfPredicate toPredicate(boolean inversed) {
    return () -> {
      StringBuilder sb = new StringBuilder();
      sb.append(first.toPredicate(inversed).getQueryString());

      if (!(PropertyPathHelper.isNil(rest))) {
        sb.append("/").append(rest.toPredicate(inversed).getQueryString());
      }

      return sb.toString();
    };
  }
}
