package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

@Builder
@Getter
@Setter
public class ZeroOrMorePath implements PropertyPath {

  private final PropertyPath object;

  @Override
  public Optional<Value> resolvePath(Model model, Resource subject) {
    throw new IllegalArgumentException("Not yet implemented");
  }
}