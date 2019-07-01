package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Builder
@Getter
@Setter
public class InversePath implements PropertyPath {

  private final PropertyPath object;

  @Override
  public Set<Value> resolvePath(Model model, Resource subject, boolean inversed) {
    return object.resolvePath(model, subject, !inversed);
  }

  @Override
  public RdfPredicate toPredicate() {
    return () -> "^(" + object.toPredicate()
        .getQueryString() + ")";
  }
}
