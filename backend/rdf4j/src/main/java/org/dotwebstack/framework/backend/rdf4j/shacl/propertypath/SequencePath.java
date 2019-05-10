package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;

@Builder
@Getter
@Setter
public class SequencePath implements PropertyPath {

  private final PropertyPath first;

  private final PropertyPath rest;

  @Override
  public Optional<Value> resolvePath(Model model, Resource subject) {
    if (this.first instanceof PredicatePath) {
      Optional<Value> o = Models.getProperty(model, subject, ((PredicatePath) this.first).getIri());
      if (o.isPresent() && (o.get() instanceof BNode)) {
        subject = (Resource) o.get();
        return rest.resolvePath(model, subject);
      }

      return o;
    } else if (this.first instanceof InversePath) {
      Optional<Value> o = this.first.resolvePath(model, subject);
      if (o.isPresent()) {
        return this.rest.resolvePath(model, (Resource) o.get());
      }
      return Optional.empty();
    }
    throw new IllegalArgumentException("Not yet implemented");
  }
}
