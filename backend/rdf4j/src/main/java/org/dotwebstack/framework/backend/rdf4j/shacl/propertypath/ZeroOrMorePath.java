package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Builder
@Getter
@Setter
public class ZeroOrMorePath implements PropertyPath {

  private final PredicatePath object;

  @Override
  public Set<Value> resolvePath(Model model, Resource subject, boolean inverse) {
    return Models.getProperties(model, subject, object.getIri());
  }

  @Override
  public RdfPredicate toPredicate() {
    return () -> "(" + object.toPredicate().getQueryString() + ")*";
  }
}