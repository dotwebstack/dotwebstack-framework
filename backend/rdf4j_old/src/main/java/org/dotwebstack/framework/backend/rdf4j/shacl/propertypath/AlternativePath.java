package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Builder
@Getter
@Setter
public class AlternativePath extends BasePath {

  private final SequencePath object;

  @Override
  public RdfPredicate toPredicate() {
    return () -> String.format("(%s)", this.getChildren()
        .stream()
        .map(child -> child.toPredicate()
            .getQueryString())
        .collect(Collectors.joining("|")));
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
