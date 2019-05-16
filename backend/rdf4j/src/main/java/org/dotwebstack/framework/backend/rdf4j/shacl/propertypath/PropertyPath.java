package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Set;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

public interface PropertyPath {

  Set<Value> resolvePath(Model model, Resource subject, boolean inversed);

  RdfPredicate toPredicate();

}
