package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Optional;
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
  public Optional<Value> resolvePath(Model model, Resource subject, boolean inversed) {
    return this.first.resolvePath(model, subject, inversed)
        .map(value -> resolveRest(model, value, inversed));
  }

  private Value resolveRest(Model model, Value value, Boolean inversed) {
    if (!PropertyPathHelper.isNil(rest) && value instanceof BNode) {
      return rest.resolvePath(model, (Resource) value, inversed).orElse(null);
    }
    return value;
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
