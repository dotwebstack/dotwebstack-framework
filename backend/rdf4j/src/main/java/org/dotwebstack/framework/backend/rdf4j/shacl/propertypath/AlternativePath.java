package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Builder
@Getter
@Setter
public class AlternativePath implements PropertyPath {

  private final SequencePath object;

  @Override
  public Set<Value> resolvePath(Model model, Resource subject, boolean inversed) {
    return getChildren().stream()
        .map(child -> child.resolvePath(model, subject, inversed))
        .filter(set -> !set.isEmpty())
        .findFirst()
        .orElse(Collections.emptySet());
  }

  @Override
  public IRI resolvePathIri(boolean inversed) {
    return object.resolvePathIri(inversed);
  }

  @Override
  public RdfPredicate toPredicate() {
    return () -> String.join("|", this.getChildren()
        .stream()
        .map(child -> child.toPredicate()
            .getQueryString())
        .collect(Collectors.toList()));
  }

  // Return the direct children of the underlying SequencePath
  private List<PropertyPath> getChildren() {
    SequencePath next = this.object;
    List<PropertyPath> children = new ArrayList<>();

    while (true) {
      children.add(next.getFirst());
      if (!(next.getRest() instanceof SequencePath)) {
        children.add(next.getRest());
        break;
      }
      next = (SequencePath) next.getRest();
    }
    return children;
  }
}
