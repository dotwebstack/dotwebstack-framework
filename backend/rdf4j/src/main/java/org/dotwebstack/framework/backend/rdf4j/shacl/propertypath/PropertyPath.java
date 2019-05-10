package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Optional;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

public interface PropertyPath {

  Optional<Value> resolvePath(Model model, Resource subject);

}
