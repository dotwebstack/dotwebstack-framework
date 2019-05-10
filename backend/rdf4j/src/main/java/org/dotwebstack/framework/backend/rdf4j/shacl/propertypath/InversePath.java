package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;

@Builder
@Getter
@Setter
public class InversePath implements PropertyPath {

  private final PredicatePath object;

  @Override
  public Optional<Value> resolvePath(Model model, Resource subject) {
    Optional<Resource> o = Models.subject(model.filter(null, object.getIri(), subject));
    if (o.isPresent()) {
      // cast resource to value...
      return Optional.of(o.get());
    }
    return Optional.empty();
  }
}
