package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Optional;
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
public class OneOrMorePath implements PropertyPath {

  private final PredicatePath object;

  @Override
  public Optional<Value> resolvePath(Model model, Resource subject) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public RdfPredicate toPredicate() {
    throw new UnsupportedOperationException("Not yet implemented.");
  }
}
