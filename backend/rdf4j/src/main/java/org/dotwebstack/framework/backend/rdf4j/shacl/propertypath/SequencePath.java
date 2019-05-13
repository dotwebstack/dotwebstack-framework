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
      return Models.getProperty(model, subject, ((PredicatePath) this.first).getIri())
          .map(value -> resolveRest(model, value));
    }

    if (this.first instanceof InversePath) {
      return this.first.resolvePath(model, subject)
          .map(value -> resolveRest(model, value));
    }

    throw new UnsupportedOperationException("Not yet implemented.");
  }

  private Value resolveRest(Model model, Value value) {
    return value instanceof BNode ? rest.resolvePath(model, (Resource) value).orElse(null) : value;
  }
}
